package MMOSystem.managers.collect;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.managers.SkillManager;
import MMOSystem.managers.sound.SoundManager;
import MMOSystem.managers.sound.SoundType;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.particle.HappyVillagerParticle;
import cn.nukkit.potion.Effect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ExcavationManager extends Manager {

	private HashMap<String, Integer> xp = new HashMap<>();

	private HashMap<Integer, HashMap<String, Object>> tr= new HashMap<>();

	private static ExcavationManager excavationmanager;

	private static ExcavationSkillManager skillmanager;

	public final MMOSystem plugin;

	public static String getJobName(){
		return "excavation";
	}

	public static String getCNName(){
		return "挖掘";
	}

	private static String getExplorerName(){
		return "发现宝藏";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f" + ExcavationSkillManager.getSkillName() + "(潜行状态下使用锹挖掘触发)\n";
		d = d + "§6<被动技能>§7：§f" + getExplorerName() + "\n";
		d = d + "§6<经验计算>§7：§f挖掘\n";
		d = d + "§6<等级作用>§7：§f增加被动触发几率，减少主动cd，延长主动时间\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f掘土工 §fLv300§8->§6挖掘机 §fLv600§8->§6大探险家 §fLv1000§8->§8获得§c▲§8标记";
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

	public static ExcavationManager getInstance(){
		return excavationmanager;
	}

	public ExcavationManager(MMOSystem plugin){
		this.plugin = plugin;
		excavationmanager = this;
		skillmanager = new ExcavationSkillManager(this);
		xp.put("2:0",30);
		xp.put("3:0",30);
		xp.put("12:0",30);
		xp.put("12:1",30);
		xp.put("13:0",45);
		xp.put("78:0",45);
		xp.put("82:0",50);
		xp.put("88:0",40);
		xp.put("110:0",40);
		xp.put("172:0",50);
		xp.put("198:0",35);
		xp.put("243:0", 45);

		int line = 0;

		for(String key : plugin.trconfig.getSection(getJobName()).keySet()){
			tr.put(line++, plugin.trconfig.getSection(getJobName()).getSection(key));
		}


	}

	public Item[] handleBreak(Player p, Block block, Item[] drops){
		ArrayList<Item> newdrops = new ArrayList<>(Arrays.asList(drops));
		plugin.addXp(p, getJobName(), calculateXP(p, block));
		if(canActiveDouble(p)){
			for(Item drop : drops){
				newdrops.add(drop);
			}
		}
		if(canActiveExplorer(p)){
			Item item = getRandomTreasure(p);
			if(item != null){
				p.getLevel().addParticle(new HappyVillagerParticle(block.add(0.5, 0.5, 0.5)));
				newdrops.add(item);
				//p.sendPopup(plugin.getMessage("passive-skill", new String[]{"{skillname}"}, new String[]{getExplorerName()}));
			}
		}
		if(p.isSneaking() && p.getInventory().getItemInHand().isShovel()){
			skillmanager.putUsingSkill(p);
		}
		return newdrops.toArray(new Item[0]);
	}

	public boolean isSoil(Block block){
		return xp.containsKey(block.getId() + ":" + block.getDamage());
	}

	public int calculateXP(Player p , Block block){
		int original_xp = getBlockXP(block);
		if(original_xp == 0){
			return original_xp;
		}else{
			return (int)(original_xp * 0.7);
		}
	}

	private int getBlockXP(Block block){
		String id = block.getId() + ":" + block.getDamage();
		return xp.getOrDefault(id, 0);
	}

	private boolean canActiveDouble(Player p){
		return Tool.YorN(getActiveDoubleChance(p));
	}

	private boolean canActiveExplorer(Player p){
		return Tool.YorN(getActiveExplorerChance(p));
	}

	private double getActiveDoubleChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return Math.sqrt(level * 10);
	}

	private double getActiveExplorerChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return Math.sqrt(level * 10);
	}

	private Item getRandomTreasure(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		HashMap<String, Object> treasure = tr.get(Tool.getRand(0, tr.size()-1));
		if(level < (int)treasure.getOrDefault("Drop_Level", 0)){
			return null;
		}
		if(Tool.YorN(Double.parseDouble(treasure.getOrDefault("Drop_Chance", 0).toString()))){
			return Item.fromString((String)treasure.getOrDefault("ID", "260:0:1"));
		}else{
			return null;
		}
	}

}

class ExcavationSkillManager extends SkillManager {

	public static String getSkillName(){
		return "人形挖掘机";
	}

	private ExcavationManager excavationmanager;

	private HashMap<Player, Integer> using = new HashMap<>();   //默认0，启动时1，冷却中2，冷却结束0

	public ExcavationSkillManager(ExcavationManager manager){
		this.excavationmanager = manager;
	}

	public void putUsingSkill(Player p){
		if(isInSkillCd(p)){
			p.sendPopup(excavationmanager.plugin.getMessage("skill-incd", new String[]{"{skillname}"}, new String[]{getSkillName()}));
			return;
		}
		if(isUsingSkill(p)){
			return;
		}
		using.put(p, 1);
		//SoundManager.sendSound(p, SoundType.ROLL_ACTIVATED);
		Tool.spawnFirework(p);
		int duration = getSkillDuration(p);
		p.sendTip(excavationmanager.plugin.getMessage("skill-start", new String[]{"{skillname}", "{duration}"}, new String[]{getSkillName(), String.valueOf(duration)}));
		p.addEffect(Effect.getEffect(Effect.HASTE).setAmplifier(4).setDuration(duration * 20).setVisible(false));
		excavationmanager.plugin.getServer().getScheduler().scheduleDelayedTask(excavationmanager.plugin, ()->putInCd(p), duration * 20);
		excavationmanager.plugin.getServer().getScheduler().scheduleDelayedTask(excavationmanager.plugin, ()->removeUsingSkill(p), getSkillCd(p) * 20);
	}


	public void putInCd(Player p){
		using.put(p, 2);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.POP);
			p.sendTip(excavationmanager.plugin.getMessage("skill-tocd", new String[]{"{skillname}", "{cd}"}, new String[]{getSkillName(), String.valueOf(getSkillCd(p))}));
		}
	}

	public void removeUsingSkill(Player p){
		using.put(p, 0);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.SKILL_UNLOCKED);
			p.sendTip(excavationmanager.plugin.getMessage("skill-prepared", new String[]{"{skillname}"}, new String[]{getSkillName()}));
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
		int level = excavationmanager.plugin.getLevel(p, ExcavationManager.getJobName());
		return (int)(300 - Math.sqrt(level * 55));
		/**
		 影响因子：等级
		 0级----400秒
		 50级--300秒
		 100级--200秒
		 500级--150秒
		 1000级---100秒
		 **/
	}


	public int getSkillDuration(Player p){
		int level = excavationmanager.plugin.getLevel(p, ExcavationManager.getJobName());
		if(level < 50){
			return 4;
		}else if(level <100){
			return 5;
		}else if(level < 200){
			return 6;
		}else if(level < 300){
			return 7;
		}else if(level < 400){
			return 8;
		}else if(level < 500){
			return 9;
		}else if(level < 600){
			return 10;
		}else if(level < 700){
			return 11;
		}else if(level < 800){
			return 12;
		}else if(level < 900){
			return 13;
		}else if(level < 1000){
			return 14;
		}else{
			return 15;
		}

	}


}
