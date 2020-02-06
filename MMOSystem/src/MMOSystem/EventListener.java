package MMOSystem;

import MMOSystem.events.LevelUpEvent;
import MMOSystem.managers.collect.*;
import MMOSystem.managers.damage.ArrowManager;
import MMOSystem.managers.damage.AxeManager;
import MMOSystem.managers.damage.SwordManager;
import MMOSystem.managers.sound.SoundManager;
import MMOSystem.managers.sound.SoundType;
import MMOSystem.managers.sundry.AcrobaticsManager;
import MMOSystem.managers.sundry.AcrobaticsManager.ResultType;
import MMOSystem.utils.BlockMetadata;
import MMOSystem.utils.BlockUtils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.EntityCreature;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.ExplosionPrimeEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFishingRod;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.potion.Effect;

import static cn.nukkit.entity.Entity.DATA_FLAGS;
import static cn.nukkit.entity.Entity.DATA_FLAG_ACTION;

public class EventListener implements Listener {

    private final MMOSystem plugin;

    public EventListener(MMOSystem plugin){
        this.plugin = plugin;
        new BlockMetadata(plugin);
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(plugin.getData("mmo", "account", p.getName()) == null){
            plugin.registerPlayer(p);
        }
        plugin.loginPlayer(p);
        AcrobaticsManager.getInstance().handleSpawn(p);
        GardeningManager.getInstance().handleSpawn(p);
        FarmingManager.getInstance().handleSpawn(p);
        /*
        Object health = plugin.getData("basis", "account", p.getName(), "health");
        if(health != null){
            p.setHealth((float)health);
        }
         */
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onQuit(PlayerQuitEvent e){
        plugin.logoutPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e){
        if(e.isCancelled()){
            return;
        }
        Player p = e.getPlayer();
        if(!p.isOnGround()){
            return;
        }
        Block block = e.getBlock();
        if(BlockUtils.isMarkedBlock(block)){
            return;
        }
        if(BlockUtils.isMineral(block)){
            e.setDrops(MiningManager.getInstance().handleBreak(p, block, e.getDrops()));
            BlockUtils.markBlock(e.getBlock());
            return;
        }
        if(BlockUtils.isHarvest(block)){
            e.setDrops(FarmingManager.getInstance().handleBreak(p, block, e.getDrops()));
            BlockUtils.markBlock(e.getBlock());
            return;
        }
        if(BlockUtils.isSoil(block)){
            e.setDrops(ExcavationManager.getInstance().handleBreak(p, block, e.getDrops()));
            BlockUtils.markBlock(e.getBlock());
            return;
        }
        if(BlockUtils.isWood(block)){
            e.setDrops(WoodcuttingManager.getInstance().handleBreak(p, block, e.getDrops()));
            BlockUtils.markBlock(e.getBlock());
            return;
        }
        if(BlockUtils.isLeaf(block) || BlockUtils.isFlower(block) || BlockUtils.isGrass(block)){
            e.setDrops(GardeningManager.getInstance().handleBreak(p, block, e.getDrops()));
            BlockUtils.markBlock(e.getBlock());
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e){
        if(e.isCancelled()){
            return;
        }
        if(!BlockUtils.isPlantable(e.getItem())){
            BlockUtils.markBlock(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEat(PlayerEatFoodEvent e){
        if(e.isCancelled()){
            return;
        }
        FarmingManager.getInstance().handleEat(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onDamage(EntityDamageByEntityEvent e){
        if(e.isCancelled()){
            return;
        }
        if(e.getDamager() instanceof Player){
            if(e.getEntity() instanceof EntityCreature || e.getEntity() instanceof EntityLiving){
                Player p = (Player)e.getDamager();
                if(p.getInventory().getItemInHand().isSword()){
                    SwordManager.getInstance().handleDamage(p, e);
                }
                if(p.getInventory().getItemInHand().isAxe()){
                    AxeManager.getInstance().handleDamage(p, e);
                }
                if(p.getInventory().getItemInHand().getId() == Item.BOW){
                    ArrowManager.getInstance().handleDamage(p, e);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBeDamaged(EntityDamageEvent e){
        if(e.isCancelled()){
            return;
        }
        if(e.getEntity() instanceof Player){
            Player p = (Player)e.getEntity();
            if(p.getHealth() - e.getFinalDamage() <= 0) {
                EntityDamageEvent cause = p.getLastDamageCause();
                if (!e.isCancelled() && cause != null && cause.getCause() != EntityDamageEvent.DamageCause.VOID) {
                    if (p.getInventory().getItem(1).getId() == 450) {
                        p.getInventory().remove(Item.get(Item.TOTEM, 0, 1));
                        LevelEventPacket pk1 = new LevelEventPacket();
                        pk1.evid = LevelEventPacket.EVENT_SOUND_TOTEM;
                        pk1.x = (float) p.x;
                        pk1.y = (float) p.y;
                        pk1.z = (float) p.z;
                        plugin.getServer().getDefaultLevel().addChunkPacket(p.getFloorX() >> 4, p.getFloorZ() >> 4, pk1);
                        p.extinguish();
                        p.setHealth(p.getMaxHealth() / 4);

                        p.addEffect(Effect.getEffect(Effect.REGENERATION).setDuration(20 * 40).setAmplifier(2));
                        p.addEffect(Effect.getEffect(Effect.ABSORPTION).setDuration(20 * 5).setAmplifier(2));

                        EntityEventPacket pk2 = new EntityEventPacket();
                        pk2.eid = p.getId();
                        pk2.event = 65;
                        p.dataPacket(pk2);

                        e.setCancelled(true);
                        return;
                    }
                }
            }
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
                ResultType result;
                if(e.getDamage() > p.getHealth()){
                    result = AcrobaticsManager.getInstance().handleFall(p, p.getHealth());
                }else{
                    result = AcrobaticsManager.getInstance().handleFall(p, e.getDamage());
                }
                switch (result){
                    case FLYER:
                        e.setCancelled();
                        e.setDamage(0);
                        break;
                    case DANCER:
                        e.setDamage(e.getDamage() / 2);
                        break;
                    default:
                        break;

                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerInteractEvent e){
        Item item = e.getItem();
        if(item instanceof ItemFishingRod){
            Player p = e.getPlayer();
            if (p.fishing != null) {
                if(p.isSurvival()){
                    e.setCancelled();
                    FishingManager.getInstance().handleFish(p.fishing);
                    p.stopFishing(false);
                    p.setDataFlag(DATA_FLAGS, DATA_FLAG_ACTION, true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onLevelUp(LevelUpEvent e){
        Player p = e.getPlayer();
        if(p != null){
            SoundManager.sendSound(e.getPlayer(), SoundType.LEVEL_UP);
            p.sendMessage(plugin.getMessage("levelup", new String[]{"{level}", "{jobname}"}, new String[]{String.valueOf(e.getLevel()), plugin.getJobCNName(e.getJob())}));
            if(e.getJob().equals("sword") || e.getJob().equals("axe") || e.getJob().equals("arrow")){
                p.addEffect(Effect.getEffect(Effect.REGENERATION).setAmplifier(1).setDuration(3*15).setVisible(true));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplode(ExplosionPrimeEvent e){
        if(e.getEntity() instanceof EntityPrimedTNT){
            e.setBlockBreaking(true);
        }else{
            e.setBlockBreaking(false);
        }
    }

}
