package MMOSystem.managers.sound;

import MMOSystem.managers.Manager;
import cn.nukkit.Player;
import cn.nukkit.level.Sound;

public class SoundManager extends Manager{

    public static void sendSound(Player player, SoundType soundType)
    {
        if(player != null){
            player.getLevel().addSound(player, getSound(soundType));
        }
    }

    private static Sound getSound(SoundType soundType)
    {
        switch(soundType)
        {
            case POP:
                return Sound.BUBBLE_POP;
            case LEVEL_UP:
                return Sound.RANDOM_LEVELUP;
            case TOOL_READY:
                return Sound.ARMOR_EQUIP_GOLD;
            case ROLL_ACTIVATED:
                return Sound.MOB_LLAMA_SWAG;
            case SKILL_UNLOCKED:
                return Sound.BLOCK_COMPOSTER_READY;
            case ABILITY_ACTIVATED_BERSERK:
                return Sound.CONDUIT_AMBIENT;
            case ABILITY_ACTIVATED_GENERIC:
                return Sound.ITEM_TRIDENT_RIPTIDE_3;
            case DEFLECT_ARROWS:
                return Sound.BLOCK_CHORUSFLOWER_DEATH;
            case TIRED:
                return Sound.CONDUIT_AMBIENT;
            case BLEED:
                return Sound.MOB_ENDERMEN_DEATH;
            case WATER:
                return Sound.LIQUID_WATER;
            default:
                return null;
        }
    }
}
