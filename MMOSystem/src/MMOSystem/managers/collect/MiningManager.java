package MMOSystem.managers.collect;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.managers.SkillManager;
import MMOSystem.managers.sound.SoundManager;
import MMOSystem.managers.sound.SoundType;
import MMOSystem.utils.BlockUtils;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.particle.HappyVillagerParticle;
import cn.nukkit.potion.Effect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class MiningManager extends Manager{


	private HashMap<String, Integer> xp = new HashMap<>();

	private static MiningManager miningmanager;

	private static MiningSkillManager skillmanager;

	public final MMOSystem plugin;

	public static MiningManager getInstance(){
		return miningmanager;
	}

	public static String getJobName(){
		return "mining";
	}

	public static String getCNName(){
		return "挖矿";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f" + MiningSkillManager.getSkillName() + "(潜行状态下使用镐子挖矿触发)\n";
		d = d + "§6<被动技能>§7：§f更多掉落\n";
		d = d + "§6<经验计算>§7：§f挖矿\n";
		d = d + "§6<等级作用>§7：§f增加被动触发几率，减少主动cd，延长主动时间\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f矿工 §fLv300§8->§b采矿机 §fLv600§8->§b挖穿星球 §fLv1000§8->§8获得§c▲§8标记";
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

	public MiningManager(MMOSystem plugin){
		this.plugin = plugin;
		miningmanager = this;
		skillmanager = new MiningSkillManager(this);
		xp.put("14:0", 120);
		xp.put("15:0", 60);
		xp.put("16:0", 30);
		xp.put("21:0", 80);
		xp.put("49:0", 150);
		xp.put("56:0", 240);
		xp.put("73:0", 50);
		xp.put("74:0", 50);
		xp.put("129:0", 500);
		xp.put("153:0", 50);
		xp.put("1:0", 0);
		xp.put("1:1", 30);
		xp.put("1:2", 30);
		xp.put("1:3", 30);
		xp.put("1:4", 30);
		xp.put("1:5", 30);
		xp.put("1:6", 30);
		xp.put("24:0", 20);
		xp.put("24:1", 20);
		xp.put("24:2", 25);
		xp.put("89:0", 50);
		xp.put("121", 30);
		/*
		xp.put("159:0",18);
		xp.put("159:1",18);
		xp.put("159:2",18);
		xp.put("159:3",18);
		xp.put("159:4",18);
		xp.put("159:5",18);
		xp.put("159:6",18);
		xp.put("159:7",18);
		xp.put("159:8",18);
		xp.put("159:9",18);
		xp.put("159:10",18);
		xp.put("159:11",18);
		xp.put("159:12",18);
		xp.put("159:13",18);
		xp.put("159:14",18);
		xp.put("159:15",18);

		 */
		xp.put("179:0", 25);
	}

	public Item[] handleBreak(Player p, Block block, Item[] drops){
		plugin.addXp(p, getJobName(), calculateXP(p, block));
		ArrayList<Item> newdrops = new ArrayList<>(Arrays.asList(drops));
		if(BlockUtils.isOre(block)){
			p.getLevel().addParticle(new HappyVillagerParticle(block.add(0.5, 0.5, 0.5)));
			if(canActiveExplorer(p)){
				for(Item drop : drops){
					newdrops.add(drop);
				}
			}
		}
		if(p.isSneaking() && p.getInventory().getItemInHand().isPickaxe()){
			skillmanager.putUsingSkill(p);
		}
		return newdrops.toArray(new Item[0]);
	}


	public boolean isMineral(Block block){
		return xp.containsKey(block.getId() + ":" + block.getDamage());
	}

	public int calculateXP(Player p , Block block){
		int original_xp = getBlockXP(block);
		if(original_xp == 0){
			return original_xp;
		}else{
			return (int)(original_xp * 0.6);
		}
	}

	public int getBlockXP(Block block){
		String id = block.getId() + ":" + block.getDamage();
		return xp.getOrDefault(id, 0);
	}

	public boolean canActiveExplorer(Player p){
		return Tool.YorN(getActiveChance(p));
	}

	public double getActiveChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return Math.sqrt(level * 8);
	}

}


class MiningSkillManager extends SkillManager{

	public static String getSkillName(){
		return "超级矿工";
	}

	private MiningManager miningmanager;

	private HashMap<Player, Integer> using = new HashMap<>();   //默认0，启动时1，冷却中2，冷却结束0

	public MiningSkillManager(MiningManager manager){
		this.miningmanager = manager;
	}

	public void putUsingSkill(Player p){
		if(isInSkillCd(p)){
			p.sendPopup(miningmanager.plugin.getMessage("skill-incd", new String[]{"{skillname}"}, new String[]{getSkillName()}));
			return;
		}
		if(isUsingSkill(p)){
			return;
		}
		using.put(p, 1);
		//SoundManager.sendSound(p, SoundType.ROLL_ACTIVATED);
		Tool.spawnFirework(p);
		int duration = getSkillDuration(p);
		p.sendTip(miningmanager.plugin.getMessage("skill-start", new String[]{"{skillname}", "{duration}"}, new String[]{getSkillName(), String.valueOf(duration)}));
		p.addEffect(Effect.getEffect(Effect.HASTE).setAmplifier(5).setDuration(duration * 20).setVisible(false));
		miningmanager.plugin.getServer().getScheduler().scheduleDelayedTask(miningmanager.plugin, ()->putInCd(p), duration * 20);
		miningmanager.plugin.getServer().getScheduler().scheduleDelayedTask(miningmanager.plugin, ()->removeUsingSkill(p), getSkillCd(p) * 20);
	}


	public void putInCd(Player p) {
		using.put(p, 2);
		if (p.isOnline()) {
			SoundManager.sendSound(p, SoundType.POP);
			p.sendTip(miningmanager.plugin.getMessage("skill-tocd", new String[]{"{skillname}", "{cd}"}, new String[]{getSkillName(), String.valueOf(getSkillCd(p))}));
		}
	}

	public void removeUsingSkill(Player p){
		using.put(p, 0);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.SKILL_UNLOCKED);
			p.sendTip(miningmanager.plugin.getMessage("skill-prepared", new String[]{"{skillname}"}, new String[]{getSkillName()}));
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
		int level = miningmanager.plugin.getLevel(p, MiningManager.getJobName());
		return (int)(280 - Math.sqrt(level * 40));
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
		int level = miningmanager.plugin.getLevel(p, MiningManager.getJobName());
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
		}else if(level < 800){
			return 10;
		}else if(level < 1000){
			return 10;
		}else{
			return 10;
		}

	}


}
