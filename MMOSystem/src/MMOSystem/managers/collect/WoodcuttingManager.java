package MMOSystem.managers.collect;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.managers.SkillManager;
import MMOSystem.managers.sound.SoundManager;
import MMOSystem.managers.sound.SoundType;
import MMOSystem.tasks.ProcessTreeTask;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.particle.HappyVillagerParticle;

import java.util.*;
public class WoodcuttingManager extends Manager {

	private HashMap<String, Integer> xp = new HashMap<>();

	private static WoodcuttingManager woodcuttingmanager;

	private static WoodcuttingSkillManager skillmanager;

	public final MMOSystem plugin;

	public static String getJobName(){
		return "woodcutting";
	}

	public static String getCNName(){
		return "伐木";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f" + WoodcuttingSkillManager.getSkillName() + "(潜行状态下使用斧头伐木触发)\n";
		d = d + "§6<被动技能>§7：§f更多掉落\n";
		d = d + "§6<经验计算>§7：§f伐木\n";
		d = d + "§6<等级作用>§7：§f增加被动触发几率，减少主动cd，延长主动时间\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f伐木工 §fLv300§8->§2林木大亨 §fLv600§8->§2幽林隐士 §fLv1000§8->§8获得§c▲§8标记";
		return d;
	}

	@Override
	public String getExpInfo(Player p){
		int xp = plugin.getXp(p, getJobName());
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§e当前经验值§f：" + xp % 1000 + " §7/ 1000\n";
		d = d + "§e当前等级§f：" + xp / 1000 + " §7/ 1000";
		return d;
	}

	public static WoodcuttingManager getInstance(){
		return woodcuttingmanager;
	}

	public WoodcuttingManager(MMOSystem plugin){
		this.plugin = plugin;
		woodcuttingmanager = this;
		skillmanager = new WoodcuttingSkillManager(this);
		xp.put("17:0",60);
		xp.put("17:1",60);
		xp.put("17:2",60);
		xp.put("17:3",60);
		xp.put("162:0",60);
		xp.put("162:1",60);
	}

	public Item[] handleBreak(Player p, Block block, Item[] drops){
		ArrayList<Item> newdrops = new ArrayList<>(Arrays.asList(drops));
		plugin.addXp(p, getJobName(), calculateXP(p, block));
		p.getLevel().addParticle(new HappyVillagerParticle(block.add(0.5, 0.5, 0.5)));
		if(canActiveExplorer(p)){
			for(Item drop : drops){
				newdrops.add(drop);
			}
		}
		if(p.isSneaking() && p.getInventory().getItemInHand().isAxe()){
			skillmanager.putUsingSkill(p);
		}
		if(skillmanager.isUsingSkill(p)){
			plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new ProcessTreeTask(p, block));
		}
		return newdrops.toArray(new Item[0]);
	}


	public boolean isWood(Block block){
		return xp.containsKey(block.getId() + ":" + block.getDamage());
	}

	private int calculateXP(Player p , Block block){
		int original_xp = getBlockXP(block);
		if(original_xp == 0){
			return original_xp;
		}else{
			return (int)(original_xp  * plugin.xpconfig.getSection("Multiply").getDouble(getJobName()));
		}
	}

	private int getBlockXP(Block block){
		String id = block.getId() + ":" + block.getDamage();
		return xp.getOrDefault(id, 0);
	}

	private boolean canActiveExplorer(Player p){
		return Tool.YorN(getActiveChance(p));
	}

	private double getActiveChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return Math.sqrt(level * 10);
	}

}

class WoodcuttingSkillManager extends SkillManager{

	public static String getSkillName(){
		return "森林保卫者";
	}

	private WoodcuttingManager woodcuttingmanager;

	private HashMap<Player, Integer> using = new HashMap<>();   //默认0，启动时1，冷却中2，冷却结束0

	public WoodcuttingSkillManager(WoodcuttingManager manager){
		this.woodcuttingmanager = manager;
	}

	public void putUsingSkill(Player p){
		if(isInSkillCd(p)){
			p.sendPopup(woodcuttingmanager.plugin.getMessage("skill-incd", new String[]{"{skillname}"}, new String[]{getSkillName()}));
			return;
		}
		if(isUsingSkill(p)){
			return;
		}
		using.put(p, 1);
		//SoundManager.sendSound(p, SoundType.ROLL_ACTIVATED);
		Tool.spawnFirework(p);
		int duration = getSkillDuration(p);
		p.sendTip(woodcuttingmanager.plugin.getMessage("skill-start", new String[]{"{skillname}", "{duration}"}, new String[]{getSkillName(), String.valueOf(duration)}));
		woodcuttingmanager.plugin.getServer().getScheduler().scheduleDelayedTask(woodcuttingmanager.plugin, ()->putInCd(p), duration * 20);
		woodcuttingmanager.plugin.getServer().getScheduler().scheduleDelayedTask(woodcuttingmanager.plugin, ()->removeUsingSkill(p), getSkillCd(p) * 20);

	}


	public void putInCd(Player p) {
		using.put(p, 2);
		if (p.isOnline()) {
			SoundManager.sendSound(p, SoundType.POP);
			p.sendTip(woodcuttingmanager.plugin.getMessage("skill-tocd", new String[]{"{skillname}", "{cd}"}, new String[]{getSkillName(), String.valueOf(getSkillCd(p))}));
		}
	}

	public void removeUsingSkill(Player p){
		using.put(p, 0);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.SKILL_UNLOCKED);
			p.sendTip(woodcuttingmanager.plugin.getMessage("skill-prepared", new String[]{"{skillname}"}, new String[]{getSkillName()}));
		}
	}


	public boolean isUsingSkill(Player p){
		if(using.getOrDefault(p, 0) == 1){
			return true;
		}
		return false;
	}

	public boolean isInSkillCd(Player p){
		if(using.getOrDefault(p, 0) == 2){
			return true;
		}
		return false;
	}


	public int getSkillCd(Player p){
		int level = woodcuttingmanager.plugin.getLevel(p, WoodcuttingManager.getJobName());
		return (int)(300 - Math.sqrt(level * 55));
		/**
		 0级----260秒
		 50级--200秒
		 100级--180秒
		 500级--100秒
		 1000级---60秒
		 **/
	}


	public int getSkillDuration(Player p){
		int level = woodcuttingmanager.plugin.getLevel(p, WoodcuttingManager.getJobName());
		if(level < 50){
			return 6;
		}else if(level <100){
			return 7;
		}else if(level < 200){
			return 8;
		}else if(level < 300){
			return 9;
		}else if(level < 400){
			return 10;
		}else if(level < 500){
			return 11;
		}else if(level < 800){
			return 12;
		}else if(level < 1000){
			return 13;
		}else{
			return 14;
		}

	}


}
