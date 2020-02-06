package MMOSystem.managers;

import cn.nukkit.Player;

public abstract class SkillManager{

	public abstract void putUsingSkill(Player p);

	public abstract void putInCd(Player p);

	public abstract void removeUsingSkill(Player p);

	public abstract boolean isUsingSkill(Player p);

	public abstract boolean isInSkillCd(Player p);

	public abstract int getSkillCd(Player p);

	public abstract int getSkillDuration(Player p);

}
