package MMOSystem.managers.collect;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.utils.CustomFishing;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityFishingHook;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.EntityEventPacket;

import java.util.Random;

public class FishingManager extends Manager{


	private static FishingManager fishingmanager;

	public final MMOSystem plugin;

	public static FishingManager getInstance(){
		return fishingmanager;
	}

	public static String getJobName(){
		return "fishing";
	}

	public static String getCNName(){
		return "钓鱼";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<被动技能>§7：§f更高概率获得稀有物品\n";
		d = d + "§6<被动技能>§7：§f钓到更稀有的物品\n";
		d = d + "§6<经验计算>§7：§f钓到鱼\n";
		d = d + "§6<等级作用>§7：§f增强被动\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f捕鱼达人 §fLv300§8->§3钓鱼宗师 §fLv600§8->§3孤舟独钓 §fLv1000§8->§8获得§c▲§8标记";
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

	public FishingManager(MMOSystem plugin){
		this.plugin = plugin;
		fishingmanager = this;

	}

	public void handleFish(EntityFishingHook hook){
		if (hook.shootingEntity instanceof Player && hook.caught) {
			Player player = (Player) hook.shootingEntity;
			Object[] result = CustomFishing.getFishingResult(hook.rod, player);
			Item item;
			int xp;
			if(result != null){
				item = (Item) result[0];
				xp = (int)result[1];
			}else{
				return;
			}
			int experience = new Random().nextInt((3 - 1) + 1) + 1;
			Vector3 motion;

			if (hook.shootingEntity != null) {
				motion = hook.shootingEntity.subtract(hook).multiply(0.1);
				motion.y += Math.sqrt(hook.shootingEntity.distance(hook)) * 0.08;
			} else {
				motion = new Vector3();
			}

			CompoundTag itemTag = NBTIO.putItemHelper(item);
			itemTag.setName("Item");

			EntityItem itemEntity = new EntityItem(
					hook.level.getChunk((int) hook.x >> 4, (int) hook.z >> 4, true),
					new CompoundTag()
							.putList(new ListTag<DoubleTag>("Pos")
									.add(new DoubleTag("", hook.getX()))
									.add(new DoubleTag("", hook.getWaterHeight()))
									.add(new DoubleTag("", hook.getZ())))
							.putList(new ListTag<DoubleTag>("Motion")
									.add(new DoubleTag("", motion.x))
									.add(new DoubleTag("", motion.y))
									.add(new DoubleTag("", motion.z)))
							.putList(new ListTag<FloatTag>("Rotation")
									.add(new FloatTag("", new Random().nextFloat() * 360))
									.add(new FloatTag("", 0)))
							.putShort("Health", 5).putCompound("Item", itemTag).putShort("PickupDelay", 1));

			if (hook.shootingEntity instanceof Player) {
				itemEntity.setOwner(hook.shootingEntity.getName());
			}
			itemEntity.spawnToAll();

			if (experience > 0) {
				player.addExperience(experience);
			}

			plugin.addXp(player, getJobName(), (int)(xp * plugin.xpconfig.getSection("Multiply").getDouble(getJobName())));
		}
		if (hook.shootingEntity instanceof Player) {
			EntityEventPacket pk = new EntityEventPacket();
			pk.eid = hook.getId();
			pk.event = EntityEventPacket.FISH_HOOK_TEASE;
			Server.broadcastPacket(hook.level.getPlayers().values(), pk);
		}
	}

}
