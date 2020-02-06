package MMOSystem.utils;

import MMOSystem.MMOSystem;
import MMOSystem.managers.collect.FishingManager;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.potion.Potion;
import cn.nukkit.utils.DyeColor;

import java.util.*;

public final class CustomFishing {

    private static final Map<Selector, Float> selectors = new HashMap<>();

    public static final Selector ROOT_FISHING = RandomItem.putSelector(new Selector(RandomItem.ROOT));

    public static final Selector FISHES = RandomItem.putSelector(new Selector(ROOT_FISHING), 0.75F);
    public static final Selector TREASURES = RandomItem.putSelector(new Selector(ROOT_FISHING), 0.15F);
    public static final Selector JUNKS = RandomItem.putSelector(new Selector(ROOT_FISHING), 0.10F);
    public static final Selector FISH = RandomItem.putSelector(new ConstantItemSelector(Item.RAW_FISH, FISHES), 0.5F);
    public static final Selector SALMON = RandomItem.putSelector(new ConstantItemSelector(Item.RAW_SALMON, FISHES), 0.25F);
    public static final Selector CLOWNFISH = RandomItem.putSelector(new ConstantItemSelector(Item.CLOWNFISH, FISHES), 0.10F);
    public static final Selector PUFFERFISH = RandomItem.putSelector(new ConstantItemSelector(Item.PUFFERFISH, FISHES), 0.15F);
    public static final Selector TREASURE_ENCHANTED_BOOK = RandomItem.putSelector(new ConstantItemSelector(Item.ENCHANT_BOOK, TREASURES),  0.26F);
    public static final Selector TREASURE_BOW = RandomItem.putSelector(new ConstantItemSelector(Item.BOW, TREASURES), 0.14F);
    public static final Selector TREASURE_FISHING_ROD = RandomItem.putSelector(new ConstantItemSelector(Item.FISHING_ROD, TREASURES), 0.12F);
    public static final Selector TREASURE_NAME_TAG = RandomItem.putSelector(new ConstantItemSelector(Item.NAME_TAG, TREASURES), 0.13F);
    public static final Selector TREASURE_SADDLE = RandomItem.putSelector(new ConstantItemSelector(Item.SADDLE, TREASURES), 0.13F);
    public static final Selector TREASURE_XPBOTTLE = RandomItem.putSelector(new ConstantItemSelector(Item.EXPERIENCE_BOTTLE, TREASURES), 0.12F);
    public static final Selector TREASURE_HGK = RandomItem.putSelector(new ConstantItemSelector(445, TREASURES), 0.12F);
    public static final Selector JUNK_BOWL = RandomItem.putSelector(new ConstantItemSelector(Item.BOWL, JUNKS), 0.12F);
    public static final Selector JUNK_FISHING_ROD = RandomItem.putSelector(new ConstantItemSelector(Item.FISHING_ROD, JUNKS), 0.024F);
    public static final Selector JUNK_LEATHER = RandomItem.putSelector(new ConstantItemSelector(Item.LEATHER, JUNKS), 0.12F);
    public static final Selector JUNK_LEATHER_BOOTS = RandomItem.putSelector(new ConstantItemSelector(Item.LEATHER_BOOTS, JUNKS), 0.12F);
    public static final Selector JUNK_ROTTEN_FLESH = RandomItem.putSelector(new ConstantItemSelector(Item.ROTTEN_FLESH, JUNKS), 0.12F);
    public static final Selector JUNK_STICK = RandomItem.putSelector(new ConstantItemSelector(Item.STICK, JUNKS), 0.06F);
    public static final Selector JUNK_STRING_ITEM = RandomItem.putSelector(new ConstantItemSelector(Item.STRING, JUNKS), 0.06F);
    public static final Selector JUNK_WATTER_BOTTLE = RandomItem.putSelector(new ConstantItemSelector(Item.POTION, Potion.NO_EFFECTS, JUNKS), 0.12F);
    public static final Selector JUNK_BONE = RandomItem.putSelector(new ConstantItemSelector(Item.BONE, JUNKS), 0.12F);
    public static final Selector JUNK_INK_SAC = RandomItem.putSelector(new ConstantItemSelector(Item.DYE, DyeColor.BLACK.getDyeData(), 10, JUNKS), 0.012F);
    public static final Selector JUNK_TRIPWIRE_HOOK = RandomItem.putSelector(new ConstantItemSelector(Item.TRIPWIRE_HOOK, JUNKS), 0.12F);

    public static Object[] getFishingResult(Item rod, Player p) {
        int fortuneLevel = 0;
        int lureLevel = 0;
        if (rod != null) {
            if (rod.getEnchantment(Enchantment.ID_FORTUNE_FISHING) != null) {
                fortuneLevel = rod.getEnchantment(Enchantment.ID_FORTUNE_FISHING).getLevel();
            } else if (rod.getEnchantment(Enchantment.ID_LURE) != null) {
                lureLevel = rod.getEnchantment(Enchantment.ID_LURE).getLevel();
            }
        }
        return getFishingResult(fortuneLevel, lureLevel, MMOSystem.getPugin().getLevel(p, FishingManager.getJobName()));
    }

    public static Object[] getFishingResult(int fortuneLevel, int lureLevel, int fishingLevel){
        float treasureChance = limitRange(0, 1, 0.10f + 0.01f * fortuneLevel + 0.01f * lureLevel + 0.0006f * fishingLevel);
        float junkChance = limitRange(0, 1, 0.10f - 0.025f * fortuneLevel - 0.01f * lureLevel - 0.0003f * fishingLevel);
        float fishChance = limitRange(0, 1, 1 - treasureChance - junkChance);
        RandomItem.putSelector(TREASURES, treasureChance);
        RandomItem.putSelector(FISHES, fishChance);
        RandomItem.putSelector(JUNKS, junkChance);
        Object result = RandomItem.selectFrom(ROOT_FISHING);
        if (result instanceof ConstantItemSelector){
            Item item = ((ConstantItemSelector) result).getItem();
            if(item.getId() == Item.ENCHANTED_BOOK || item.isTool() || item.isArmor()){
                int times = Tool.getRand(1, 4);
                if(times == 4){
                    //if(item.getId() == Item.ENCHANTED_BOOK){
                    //   return new Object[]{EnchantBook.generateRandomBookItem(), 300};
                    //}else{
                        times = 2;
                    //}
                }
                List<Enchantment> enchs = Arrays.asList(Enchantment.getEnchantments());
                Collections.shuffle(enchs, new Random());
                int i = 0;
                for(Enchantment ench : enchs){
                    if(item.isTool() && ench.canEnchant(item)){
                        ench.setLevel(Tool.getRand(1, ench.getMaxLevel()));
                        item.addEnchantment(ench);
                        i = i + 1;
                        if(i >= times){
                            break;
                        }
                    }else{
                        ench.setLevel(Tool.getRand(1, ench.getMaxLevel()));
                        item.addEnchantment(ench);
                        i = i + 1;
                        if(i >= times){
                            break;
                        }
                    }
                }
                if(item.getId() != Item.ENCHANTED_BOOK){
                    item.setDamage(Tool.getRand(3, item.getMaxDurability()));
                }
            }
            int xp;
            float chance = RandomItem.selectors.getOrDefault(((ConstantItemSelector) result).getParent(), 0f);
            if(chance == treasureChance){
                xp = 340;
            }else if(chance == junkChance){
                xp = 300;
            }else{
                xp = 280;
            }
            return new Object[]{item, xp};
        }
        return null;
    }

    private static float limitRange(float min, float max, float value) {
        if (value >= max) return max;
        if (value <= min) return min;
        return value;
    }
}

class RandomItem {
    public static final Map<Selector, Float> selectors = new HashMap<>();

    public static final Selector ROOT = new Selector(null);

    public static Selector putSelector(Selector selector) {
        return putSelector(selector, 1);
    }

    public static Selector putSelector(Selector selector, float chance) {
        if (selector.getParent() == null) selector.setParent(ROOT);
        selectors.put(selector, chance);
        return selector;
    }

    static Object selectFrom(Selector selector) {
        Objects.requireNonNull(selector);
        Map<Selector, Float> child = new HashMap<>();
        selectors.forEach((s, f) -> {
            if (s.getParent() == selector) child.put(s, f);
        });
        if (child.size() == 0) return selector.select();
        return selectFrom(Selector.selectRandom(child));
    }

}
class Selector {

    private Selector parent;

    public Selector(Selector parent) {
        this.setParent(parent);
    }

    public Selector setParent(Selector parent) {
        this.parent = parent;
        return parent;
    }

    public Selector getParent() {
        return parent;
    }

    public Object select() {
        return this;
    }

    public static Selector selectRandom(Map<Selector, Float> selectorChanceMap) {
        final float[] totalChance = {0};
        selectorChanceMap.values().forEach(f -> totalChance[0] += f);
        float resultChance = (float) (Math.random() * totalChance[0]);
        final float[] flag = {0};
        final boolean[] found = {false};
        final Selector[] temp = {null};
        selectorChanceMap.forEach((o, f) -> {
            flag[0] += f;
            if (flag[0] > resultChance && !found[0]) {
                temp[0] = o;
                found[0] = true;
            }
        });
        return temp[0];
    }
}
class ConstantItemSelector extends Selector {

    protected final Item item;

    public ConstantItemSelector(int id, Selector parent) {
        this(id, 0, parent);
    }

    public ConstantItemSelector(int id, Integer meta, Selector parent) {
        this(id, meta, 1, parent);
    }

    public ConstantItemSelector(int id, Integer meta, int count, Selector parent) {
        this(new Item(id, meta, count), parent);
    }

    public ConstantItemSelector(Item item, Selector parent) {
        super(parent);
        this.item = item;
    }

    public Item getItem() {
        return item.clone();
    }

    public Object select() {
        return this;
    }
}

