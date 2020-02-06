package MMOSystem.managers.sundry;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;

public class AcrobaticsManager extends Manager {

	public enum ResultType{
		FLYER,
		DANCER,
		NORMAL
	}

	private static AcrobaticsManager acrobaticsmanager;

	private final MMOSystem plugin;

	public static String getFlyerName(){
		return "鞋底进水";
	}

	public static String getDancerName(){
		return "摔落伤害减半";
	}

	public static String getJobName(){
		return "acrobatics";
	}

	public static String getCNName(){
		return "杂技";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f潜行状态时被动触发概率提高\n";
		d = d + "§6<被动技能>§7：§f" + getDancerName() + "§7, §f" + getFlyerName() + "\n";
		d = d + "§6<经验计算>§7：§f摔落\n";
		d = d + "§6<等级作用>§7：§f提升技能触发概率， 每25级提升1格血量\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f走位新秀 §fLv300§8->§9逮虾户 §fLv600§8->§9时空旅人 §fLv1000§8->§8获得§c▲§8标记";
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

	public static AcrobaticsManager getInstance(){
		return acrobaticsmanager;
	}

	public AcrobaticsManager(MMOSystem plugin){
		this.plugin = plugin;
		acrobaticsmanager = this;
	}

	public void handleSpawn(Player p){
		p.setMaxHealth(p.getMaxHealth() + getExtraHealth(p));
	}

	public ResultType handleFall(Player p, float damage){  //两次判断，第一次触发免伤，第二次触发减伤，否则不改变
		plugin.addXp(p, getJobName(), calculateXP(p, damage));
		if(canActiveFlyer(p)){
			p.sendPopup(plugin.getMessage("passive-skill", new String[]{"{skillname}"}, new String[]{getFlyerName()}));
			return ResultType.FLYER;
		}
		if(canActiveDancer(p)){
			p.sendPopup(plugin.getMessage("passive-skill", new String[]{"{skillname}"}, new String[]{getDancerName()}));
			return ResultType.DANCER;
		}
		return ResultType.NORMAL;
	}

	public int calculateXP(Player p , float damage){
		return (int)(damage * 40);
	}


	public boolean canActiveFlyer(Player p){
		return Tool.YorN(getActiveFlyerChance(p));
	}

	public double getActiveFlyerChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		if(p.isSneaking()){
			return Math.sqrt(level * 1.5);
		}else{
			return Math.sqrt(level * 0.6);
		}
	}

	public boolean canActiveDancer(Player p){
		return Tool.YorN(getActiveDancerChance(p));
	}

	public double getActiveDancerChance(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		if(p.isSneaking()){
			return Math.sqrt(level * 4);
		}else{
			return Math.sqrt(level * 1.8);
		}
	}

	public int getExtraHealth(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return level / 25 * 2;
	}

}
