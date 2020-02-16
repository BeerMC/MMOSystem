package MMOSystem.managers.collect;

import MMOSystem.MMOSystem;
import MMOSystem.managers.Manager;
import MMOSystem.utils.BlockUtils;
import MMOSystem.utils.Tool;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.level.particle.HappyVillagerParticle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class GardeningManager extends Manager {

	//private HashMap<String, Integer> xp = new HashMap<>();

	private HashMap<String, Boolean> particles = new HashMap<>();

	private HashMap<Integer, HashMap<String, Object>> tr= new HashMap<>();

	private static GardeningManager gardeningmanager;

	private final MMOSystem plugin;

	public static String getJobName(){
		return "gardening";
	}

	public static String getCNName(){
		return "园艺";
	}

	private static String getExplorerName(){
		return "奇妙的发现";
	}

	@Override
	public String getDescription(){
		String d = "§b——————<§e" + getCNName() +"§b>——————\n";
		d = d + "§6<被动技能>§7：§f" + getExplorerName() + "\n";
		d = d + "§6<被动技能>§7：§f更多掉落\n";
		d = d + "§6<被动技能>§7：§f达到500级解锁伴随粒子特效\n";
		d = d + "§6<经验计算>§7：§f采花，除草，修叶" + "\n";
		d = d + "§6<等级作用>§7：§f提升技能触发概率， 每25级提升1格血量\n";
		//d = d + "§6<获得称号>§7：§fLv100§8->§f花匠 §fLv300§8->§d花使者 §fLv600§8->§d❀小花仙§ §fLv1000§8->§8获得§c▲§8标记";
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

	public static GardeningManager getInstance(){
		return gardeningmanager;
	}

	public GardeningManager(MMOSystem plugin){
		this.plugin = plugin;
		gardeningmanager = this;
		/*
		xp.put("18:0", 35);
		xp.put("18:1", 35);
		xp.put("18:2", 35);
		xp.put("161:0", 35);
		xp.put("161:1", 35);

		xp.put("31:0", 40);
		xp.put("31:1", 40);
		xp.put("31:2", 40);
		xp.put("31:3", 40);
		xp.put("32:0", 40);
		xp.put("111:0", 40);
		xp.put("106:1", 40);
		xp.put("106:2", 40);
		xp.put("106:4", 40);
		xp.put("106:8", 40);
		xp.put("175:2", 40);
		xp.put("175:3", 40);

		xp.put("37:0", 40);
		xp.put("38:0", 40);
		xp.put("38:1", 40);
		xp.put("38:2", 40);
		xp.put("38:3", 40);
		xp.put("38:4", 40);
		xp.put("38:5", 40);
		xp.put("38:6", 40);
		xp.put("38:7", 40);
		xp.put("38:8", 40);
		xp.put("38:9", 40);
		xp.put("38:10", 40);
		xp.put("175:0", 40);
		xp.put("175:1", 40);
		xp.put("175:4", 40);
		xp.put("175:8", 40);
		xp.put("175:5", 40);
		xp.put("175:9", 40);
		xp.put("175:12", 40);
		xp.put("175:13", 40);
		 */

		int line = 0;
        for(String key : plugin.trconfig.getSection(getJobName()).keySet()){
            tr.put(line++, plugin.trconfig.getSection(getJobName()).getSection(key));
        }
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, this::playParticle,  7);
	}


	public Item[] handleBreak(Player p, Block block, Item[] drops){
		ArrayList<Item> newdrops = new ArrayList<>(Arrays.asList(drops));
		int xp = calculateXP(p, block);
		if(xp != 0){
			plugin.addXp(p, getJobName(), xp);
			p.getLevel().addParticle(new HappyVillagerParticle(block.add(0.5, 0.5, 0.5)));
			if(canActiveDouble(p)){
				for(Item drop : drops){
					newdrops.add(drop);
				}
			}
			if(canActiveExplorer(p)){
				Item item = getRandomTreasure(p);
				if(item != null){
					newdrops.add(item);
					p.sendPopup(plugin.getMessage("passive-skill", new String[]{"{skillname}"}, new String[]{getExplorerName()}));
				}
			}
		}
		return newdrops.toArray(new Item[0]);
	}

	public void handleSpawn(Player p){
		p.setMaxHealth(p.getMaxHealth() + getExtraHealth(p));
		if(plugin.getLevel(p, getJobName()) >= 500){
			if(!particles.containsKey(p.getName())){
				particles.put(p.getName(), true);
			}
		}
	}

	public void switchParticle(Player p){
		if(plugin.getLevel(p, getJobName()) >= 500){
			boolean now = particles.getOrDefault(p.getName(), false);
			particles.put(p.getName(), !now);
			if(now){
				p.sendMessage(plugin.getMessage("particle-close"));
			}else{
				p.sendMessage(plugin.getMessage("particle-open"));
			}
		}else{
			p.sendMessage(plugin.getMessage("particle-not"));
		}
	}

	private void playParticle(){
	    for(Map.Entry<String, Boolean> entry  : particles.entrySet()){
	        if(entry.getValue()){
	        	Player p = plugin.getServer().getPlayer(entry.getKey());
	        	if(p != null){
					p.getLevel().addParticle(new HappyVillagerParticle(p.add(0, 1.2, 0)));
				}
            }
        }
    }


	public int getExtraHealth(Player p){
		int level = plugin.getLevel(p.getName(), getJobName());
		return level / 25 * 2;
	}

	public int calculateXP(Player p , Block block){
		int original_xp = getBlockXP(block);
		if(original_xp == 0){
			return original_xp;
		}else{
			return (int)(1.4 * original_xp  * plugin.xpconfig.getSection("Multiply").getDouble(getJobName()));
		}
	}

	private int getBlockXP(Block block){
		if(BlockUtils.isLeaf(block)){
			return 38;
		}
		if(BlockUtils.isGrass(block)){
			return 48;
		}
		if(BlockUtils.isFlower(block)){
			return 58;
		}
		return 0;
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
