package MMOSystem.tasks;

import MMOSystem.MMOSystem;
import MMOSystem.managers.collect.GardeningManager;
import MMOSystem.managers.collect.WoodcuttingManager;
import MMOSystem.utils.BlockUtils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.scheduler.AsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author mcMMO-Dev
 */
public class ProcessTreeTask extends AsyncTask{

	private Player p;

	private Block block;

	public ProcessTreeTask(Player p, Block block){
		this.p = p;
		this.block = block;
	}


	public void onRun(){
		processTreeFeller(p, block);
	}


	private static void processTreeFeller(Player p, Block block) {
		Set<Block> treeFellerBlocks = new HashSet<>();
		processTree(p, block, treeFellerBlocks);
		dropTreeFellerLootFromBlocks(p, treeFellerBlocks);
	}

	private static void processTree(Player p, Block block, Set<Block> treeFellerBlocks) {
		List<Block> futureCenterBlocks = new ArrayList<>();

		if (processTreeFellerTargetBlock(p, block.getSide(BlockFace.UP), futureCenterBlocks, treeFellerBlocks)) {
			for (int[] dir : directions) {
				processTreeFellerTargetBlock(p, block.level.getBlock(block.getFloorX() + dir[0], block.getFloorY(), block.getFloorZ() + dir[1]), futureCenterBlocks, treeFellerBlocks);
			}
		}
		else {
			processTreeFellerTargetBlock(p, block.getSide(BlockFace.DOWN), futureCenterBlocks, treeFellerBlocks);
			for (int y = -1; y <= 1; y++) {
				for (int[] dir : directions) {
					processTreeFellerTargetBlock(p, block.level.getBlock(block.getFloorX() + dir[0], block.getFloorY(), block.getFloorZ() + dir[1]), futureCenterBlocks, treeFellerBlocks);
				}
			}
		}
		for (Block futureCenterBlock : futureCenterBlocks) {
			processTree(p, futureCenterBlock, treeFellerBlocks);
		}
	}

	private static boolean processTreeFellerTargetBlock(Player p, Block blockState, List<Block> futureCenterBlocks, Set<Block> treeFellerBlocks) {
		if (treeFellerBlocks.contains(blockState)) {
			return false;
		}
		if (BlockUtils.isWood(blockState)) {
			treeFellerBlocks.add(blockState);
			futureCenterBlocks.add(blockState);
			return true;
		}
		else if (BlockUtils.isLeaf(blockState)) {
			treeFellerBlocks.add(blockState);
			return false;
		}
		return false;
	}


	private static void dropTreeFellerLootFromBlocks(Player player, Set<Block> treeFellerBlocks) {
		if(treeFellerBlocks.size() >= 200){
			player.sendPopup(MMOSystem.getPugin().getMessage("too-big-tree"));
			return;
		}
		int count_wood = 0;
		int count_leaf = 0;
		Item[] drops;
		for (Block block : treeFellerBlocks) {
			if (BlockUtils.isWood(block)) {
				count_wood += 1;
				block.level.dropItem(block, Item.get(block.getId(), block.getDamage()));
			}
			if (BlockUtils.isLeaf(block)) {
				count_leaf += 1;
				drops = block.getDrops(player.getInventory().getItemInHand());
				for(Item drop : drops){
					block.level.dropItem(block, drop);
				}
			}
			block.level.setBlock(block, new BlockAir());
		}
		MMOSystem.getPugin().addXp(player, WoodcuttingManager.getJobName(), count_wood * 35);
		MMOSystem.getPugin().addXp(player, GardeningManager.getJobName(), count_leaf * 20);
	}

	private static final int[][] directions = {
			new int[] {-2, -1}, new int[] {-2, 0}, new int[] {-2, 1},
			new int[] {-1, -2}, new int[] {-1, -1}, new int[] {-1, 0}, new int[] {-1, 1}, new int[] {-1, 2},
			new int[] { 0, -2}, new int[] { 0, -1},                    new int[] { 0, 1}, new int[] { 0, 2},
			new int[] { 1, -2}, new int[] { 1, -1}, new int[] { 1, 0}, new int[] { 1, 1}, new int[] { 1, 2},
			new int[] { 2, -1}, new int[] { 2, 0}, new int[] { 2, 1},
	};

}