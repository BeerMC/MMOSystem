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

public class FarmingManager extends Manager {

	private HashMap<String, Integer> xp_plant = new HashMap<>();

	private HashMap<String, Integer> xp_harvest = new HashMap<>();

	private static FarmingManager farmingmanager;

	private static FarmingSkillManager skillmanager;

	public final MMOSystem plugin;

	public static String getJobName(){
		return "farming";
	}

	public static String getCNName(){
		return "种植";
	}

	private static String getExplorerName(){
		return "粮食学家";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f" + FarmingSkillManager.getSkillName() + "(潜行状态下进行食用触发)\n";
		d = d + "§6<被动技能>§7：§f" + getExplorerName()+ "\n";
		d = d + "§6<经验计算>§7：§f收获粮食" + "\n";
		d = d + "§6<等级作用>§7：§f提升被动触发概率，减少主动cd，增强主动效果， 每25级提升1格血量\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f农民 §fLv300§8->§e粮食学家 §fLv600§8->§e养育天下 §fLv1000§8->§8获得§c▲§8标记";
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


	public static FarmingManager getInstance(){
		return farmingmanager;
	}

	public FarmingManager(MMOSystem plugin){
		this.plugin = plugin;
		farmingmanager = this;
		skillmanager = new FarmingSkillManager(this);
		/**         种植item            **/
		xp_plant.put("295:0", 30);
		xp_plant.put("361:0", 30);
		xp_plant.put("362:0", 30);
		//xp_plant.put("338:0", 30);
		xp_plant.put("392:0", 30);
		xp_plant.put("391:0", 30);
		xp_plant.put("458:0", 30);
		xp_plant.put("372:0", 30);

		/**         果实block            **/
		xp_harvest.put("39:0", 50);//蘑菇
		xp_harvest.put("40:0", 50);//蘑菇
		xp_harvest.put("81:0", 30);//仙人掌
		xp_harvest.put("83:0", 45);  //甘蔗
		xp_harvest.put("86:0", 70);  //南瓜
		xp_harvest.put("103:0", 70);//西瓜
		xp_harvest.put("59:6", 33);
		xp_harvest.put("59:7", 35);
		xp_harvest.put("115:2", 30);
		xp_harvest.put("115:3", 373);
		xp_harvest.put("127:8", 23);
		xp_harvest.put("127:9", 25);
		xp_harvest.put("127:10", 28);
		xp_harvest.put("127:11", 30);
		xp_harvest.put("141:4", 30);
		xp_harvest.put("141:5", 30);
		xp_harvest.put("141:6", 35);
		xp_harvest.put("141:7", 35);
		xp_harvest.put("142:4", 30);
		xp_harvest.put("142:5", 30);
		xp_harvest.put("142:6", 35);
		xp_harvest.put("142:7", 35);
		xp_harvest.put("244:4", 25);
		xp_harvest.put("244:5", 30);
		xp_harvest.put("244:6", 35);
		xp_harvest.put("244:7", 35);
	}

	public Item[] handleBreak(Player p, Block block, Item[] drops){
		ArrayList<Item> newdrops = new ArrayList<>(Arrays.asList(drops));
		plugin.addXp(p, getJobName(), calculateHarvestXP(p, block));
		if(canActiveExplorer(p)){
			p.getLevel().addParticle(new HappyVillagerParticle(block.add(0.5, 0.5, 0.5)));
			p.sendPopup(plugin.getMessage("passive-skill", new String[]{"{skillname}"}, new String[]{getExplorerName()}));
			for(Item drop : drops){
				newdrops.add(drop);
			}
		}
		return newdrops.toArray(new Item[0]);
	}

	public void handleEat(Player p){
		if(p.isSneaking()){
			skillmanager.putUsingSkill(p);
		}
	}

	public void handleSpawn(Player p){
		p.setMaxHealth(p.getMaxHealth() + getExtraHealth(p));
	}

	public boolean isPlantable(Item item){
		return xp_plant.containsKey(item.getId() + ":" + item.getDamage());
	}

	public boolean isHarvest(Block block){
		return xp_harvest.containsKey(block.getId() + ":" + block.getDamage());
	}

	public int calculatePlantXP(Player p , Item item){
		return (int)(0.3 * getPlantXP(item));
	}

	private int calculateHarvestXP(Player p , Block block){
		return (int)(0.3 * getHarvestXP(block) * plugin.xpconfig.getSection("Multiply").getDouble(getJobName()));
	}

	private int getPlantXP(Item item){
		String id = item.getId() + ":" + item.getDamage();
		return xp_plant.getOrDefault(id, 0);
	}

	private int getHarvestXP(Block block){
		String id = block.getId() + ":" + block.getDamage();
		return xp_harvest.getOrDefault(id, 0);
	}

	private boolean canActiveExplorer(Player p){
		return Tool.YorN(getActiveChance(p));
	}

	private double getActiveChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return Math.sqrt(level * 5);
	}

	private int getExtraHealth(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return level / 25 * 2;
	}

}




class FarmingSkillManager extends SkillManager {

	public static String getSkillName() {
		return "生命之源";
	}

	private FarmingManager farmingmanager;

	private HashMap<Player, Integer> using = new HashMap<>();   //默认0，启动时1，冷却中2，冷却结束0

	public FarmingSkillManager(FarmingManager manager) {
		this.farmingmanager = manager;
	}

	public void putUsingSkill(Player p) {
		if (isInSkillCd(p)) {
			p.sendPopup(farmingmanager.plugin.getMessage("skill-incd", new String[]{"{skillname}"}, new String[]{getSkillName()}));
			return;
		}
		if (isUsingSkill(p)) {
			return;
		}
		using.put(p, 1);
		SoundManager.sendSound(p, SoundType.ROLL_ACTIVATED);
		int duration = getSkillDuration(p);
		p.sendTip(farmingmanager.plugin.getMessage("skill-start", new String[]{"{skillname}", "{duration}"}, new String[]{getSkillName(), String.valueOf(duration)}));
		p.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(2).setDuration(duration * 20).setVisible(true));
		p.addEffect(Effect.getEffect(Effect.SATURATION).setAmplifier(2).setDuration(duration * 20).setVisible(true));
		farmingmanager.plugin.getServer().getScheduler().scheduleDelayedTask(farmingmanager.plugin, () -> putInCd(p), duration * 20);
		farmingmanager.plugin.getServer().getScheduler().scheduleDelayedTask(farmingmanager.plugin, () -> removeUsingSkill(p), getSkillCd(p) * 20);
	}


	public void putInCd(Player p) {
		using.put(p, 2);
		if (p.isOnline()) {
			SoundManager.sendSound(p, SoundType.POP);
			p.sendTip(farmingmanager.plugin.getMessage("skill-tocd", new String[]{"{skillname}", "{cd}"}, new String[]{getSkillName(), String.valueOf(getSkillCd(p))}));
		}
	}

	public void removeUsingSkill(Player p) {
		using.put(p, 0);
		if (p.isOnline()) {
			SoundManager.sendSound(p, SoundType.SKILL_UNLOCKED);
			p.sendTip(farmingmanager.plugin.getMessage("skill-prepared", new String[]{"{skillname}"}, new String[]{getSkillName()}));
		}
	}


	public boolean isUsingSkill(Player p) {
		if (using.getOrDefault(p, 0) == 1) {
			return true;
		}
		return false;
	}

	public boolean isInSkillCd(Player p) {
		if (using.getOrDefault(p, 0) == 2) {
			return true;
		}
		return false;
	}


	public int getSkillCd(Player p) {
		int level = farmingmanager.plugin.getLevel(p, FarmingManager.getJobName());
		return (int) (380 - Math.sqrt(level * 90));
		/**
		 0级----400秒
		 50级--300秒
		 100级--200秒
		 500级--150秒
		 1000级---100秒
		 **/
	}


	public int getSkillDuration(Player p) {
		int level = farmingmanager.plugin.getLevel(p, FarmingManager.getJobName());
		if (level < 50) {
			return 6;
		} else if (level < 100) {
			return 7;
		} else if (level < 200) {
			return 8;
		} else if (level < 300) {
			return 9;
		} else if (level < 400) {
			return 10;
		} else if (level < 500) {
			return 11;
		} else if (level < 800) {
			return 12;
		} else if (level < 1000) {
			return 13;
		} else {
			return 14;
		}

	}
}
