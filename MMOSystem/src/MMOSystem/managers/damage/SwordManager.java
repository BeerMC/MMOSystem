package MMOSystem.managers.damage;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.managers.SkillManager;
import MMOSystem.managers.sound.SoundManager;
import MMOSystem.managers.sound.SoundType;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.potion.Effect;

import java.util.HashMap;

public class SwordManager extends Manager{

	private static SwordManager swordmanager;

	private static SwordSkillManager skillmanager;

	public final MMOSystem plugin;

	public static SwordManager getInstance(){
		return swordmanager;
	}

	public static String getJobName(){
		return "sword";
	}

	public static String getCNName(){
		return "剑术";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<主动技能>§7：§f" + SwordSkillManager.getSkillName() + "(潜行状态下使用剑触发)\n";
		d = d + "§6<经验计算>§7：§f使用剑造成伤害\n";
		d = d + "§6<等级作用>§7：§f减少主动cd，增强主动效果\n";
        //d = d + "§6<获得称号>§7：§fLv100§8->§f习剑者 §fLv300§8->§4剑之精通 §fLv600§8->§4剑仙 §fLv1000§8->§8获得§c▲§8标记";
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

	public SwordManager(MMOSystem plugin){
		this.plugin = plugin;
		swordmanager = this;
		skillmanager = new SwordSkillManager(this);
	}


	public void handleDamage(Player p, EntityDamageByEntityEvent e){
		float damage = e.getDamage() > e.getEntity().getHealth() ? e.getEntity().getHealth() : e.getDamage();
		plugin.addXp(p, getJobName(), calculateXP(p, damage));
		if(p.isSneaking()){
			skillmanager.putUsingSkill(p);
		}
		if(skillmanager.isUsingSkill(p)){
			if(e.getEntity() instanceof Player){
				if(Tool.YorN(20)){
					((Player)e.getEntity()).sendPopup(plugin.getMessage("pvp-harming"));
					p.sendPopup(plugin.getMessage("pvp-harming-damager"));
					e.getEntity().addEffect(Effect.getEffect(Effect.HARMING).setAmplifier(0).setDuration(60).setVisible(true));
				}
			}
		}
	}


	public int calculateXP(Player p ,float damage){
		return (int)(damage * 4.5 * plugin.xpconfig.getSection("Multiply").getDouble(getJobName()));
	}

}


class SwordSkillManager extends SkillManager{

	public static String getSkillName(){
		return "剑之奥义";
	}

	private SwordManager swordmanager;

	private HashMap<Player, Integer> using = new HashMap<>();   //默认0，启动时1，冷却中2，冷却结束0

	public SwordSkillManager(SwordManager manager){
		this.swordmanager = manager;
	}

	public void putUsingSkill(Player p){
		if(isInSkillCd(p)){
			p.sendPopup(swordmanager.plugin.getMessage("skill-incd", new String[]{"{skillname}"}, new String[]{getSkillName()}));
			return;
		}
		if(isUsingSkill(p)){
			return;
		}
		using.put(p, 1);
		//SoundManager.sendSound(p, SoundType.ROLL_ACTIVATED);
		Tool.spawnFirework(p);
		int duration = getSkillDuration(p);
		p.sendTip(swordmanager.plugin.getMessage("skill-start", new String[]{"{skillname}", "{duration}"}, new String[]{getSkillName(), String.valueOf(duration)}));
		p.addEffect(Effect.getEffect(Effect.STRENGTH).setAmplifier(1).setDuration(duration * 20).setVisible(true));
		swordmanager.plugin.getServer().getScheduler().scheduleDelayedTask(swordmanager.plugin, ()->putInCd(p), duration * 20);
		swordmanager.plugin.getServer().getScheduler().scheduleDelayedTask(swordmanager.plugin, ()->removeUsingSkill(p), getSkillCd(p) * 20);
	}


	public void putInCd(Player p){
		using.put(p, 2);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.POP);
			p.sendTip(swordmanager.plugin.getMessage("skill-tocd", new String[]{"{skillname}", "{cd}"}, new String[]{getSkillName(), String.valueOf(getSkillCd(p))}));
		}
	}

	public void removeUsingSkill(Player p){
		using.put(p, 0);
		if(p.isOnline()){
			SoundManager.sendSound(p, SoundType.SKILL_UNLOCKED);
			p.sendTip(swordmanager.plugin.getMessage("skill-prepared", new String[]{"{skillname}"}, new String[]{getSkillName()}));
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
		int level = swordmanager.plugin.getLevel(p, SwordManager.getJobName());
		return (int)(320 - Math.sqrt(level * 60));
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
		int level = swordmanager.plugin.getLevel(p, SwordManager.getJobName());
		if(level < 50){
			return 5;
		}else if(level <100){
			return 7;
		}else if(level < 200){
			return 9;
		}else if(level < 300){
			return 11;
		}else if(level < 400){
			return 13;
		}else if(level < 500){
			return 15;
		}else if(level < 800){
			return 17;
		}else if(level < 1000){
			return 19;
		}else{
			return 20;
		}
	}


}
