package MMOSystem.utils;

import MMOSystem.managers.collect.ExcavationManager;
import MMOSystem.managers.collect.FarmingManager;
import MMOSystem.managers.collect.MiningManager;
import MMOSystem.managers.collect.WoodcuttingManager;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;

public final class BlockUtils {

    private final static String[] ore = {"14:0", "15:0", "16:0", "21:0", "49:0", "56:0", "73:0", "74:0", "129:0", "153:0"};

    private final static String[] leaf = {"18:0", "18:1", "18:2", "18:3","18:4","18:5","18:6","18:7","18:8","18:9", "161:0", "161:1"};

    private final static String[] grass = {"31:0", "31:1", "31:2", "31:3", "32:0","111:0", "175:2", "175:3"};

    private final static String[] flower = {"37:0", "38:0", "38:1", "38:2", "38:3", "38:4", "38:5", "38:6", "38:7", "38:8", "38:9", "38:10", "175:0", "175:1", "175:4","175:5", "175:8", "175:9", "175:12", "175:13"};

    public static boolean isMarkedBlock(Block block){
        try{
            return block.hasMetadata("mmo");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void markBlock(Block block){
        try{
            block.setMetadata("mmo", BlockMetadata.getInstance());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isOre(Block block) {//矿石
        String blockid = block.getId() + ":" + block.getDamage();
        for(String id:ore){
            if(id.equals(blockid)){
                return true;
            }
        }
        return false;
    }

    public static boolean isMineral(Block block){
        return MiningManager.getInstance().isMineral(block);
    }

    public static boolean isSoil(Block block) {//土壤
        return ExcavationManager.getInstance().isSoil(block);
    }

    public static boolean isLeaf(Block block) {//叶
        String blockid = block.getId() + ":" + block.getDamage();
        for(String id:leaf){
            if(id.equals(blockid)){
                return true;
            }
        }
        return false;
    }

    public static boolean isGrass(Block block){//草
        String blockid = block.getId() + ":" + block.getDamage();
        for(String id:grass){
            if(id.equals(blockid)){
                return true;
            }
        }
        return false;
    }

    public static boolean isFlower(Block block){//花
        String blockid = block.getId() + ":" + block.getDamage();
        for(String id:flower){
            if(id.equals(blockid)){
                return true;
            }
        }
        return false;
    }

    public static boolean isHarvest(Block block){//丰收物
        return FarmingManager.getInstance().isHarvest(block);
    }

    public static boolean isPlantable(Item item){//丰收物
        return FarmingManager.getInstance().isPlantable(item);
    }

    public static boolean isWood(Block block){//木头
        return WoodcuttingManager.getInstance().isWood(block);
    }

}
