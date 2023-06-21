package cn.powernukkitx.replaynk;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.provider.CustomClassEntityProvider;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.lang.PluginI18n;
import cn.nukkit.lang.PluginI18nManager;
import cn.nukkit.plugin.PluginBase;
import cn.powernukkitx.replaynk.command.ReplayCommand;
import cn.powernukkitx.replaynk.entity.MarkerEntity;
import cn.powernukkitx.replaynk.item.*;
import cn.powernukkitx.replaynk.trail.Trail;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author daoge_cmd
 * @date 2023/6/11
 * ReplayNK Project
 */
public final class ReplayNK extends PluginBase implements Listener {

    public static final int TRAIL_TICK_PERIOD = 20;
    private static final Map<Player, Integer> PLAYER_ACTION_TIMER = new HashMap<>();
    private static final int PLAYER_ACTION_COOL_DOWN = 1;
    @Getter
    private static ReplayNK instance;
    @Getter
    private static PluginI18n I18n;

    {
        instance = this;
    }

    @Override
    public void onLoad() {
        I18n = PluginI18nManager.register(this);
        var logger = getLogger();
        logger.info("Loading ReplayNK...");
        logger.info("Registering items...");
        registerItems();
        logger.info("Registered items.");
        logger.info("Registering entities...");
        registerEntities();
        logger.info("Registered entities.");
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(this, this);
        Server.getInstance().getCommandMap().register("", new ReplayCommand(this));
        Trail.readAllTrails();

        Server.getInstance().getScheduler().scheduleRepeatingTask(this, () -> {
            for (var trail : Trail.getTrails().values()) {
                trail.tick();
            }
        }, TRAIL_TICK_PERIOD);
    }

    @Override
    public void onDisable() {
        Trail.closeAndSave();
    }

    private void registerItems() {
        Item.registerCustomItem(List.of(
                AddMarkerItem.class,
                ClearMarkerItem.class,
                ExitItem.class,
                PauseItem.class,
                PlayItem.class,
                EditMarkerItem.class,
                SettingItem.class
        ));
    }

    private void registerEntities() {
        Entity.registerCustomEntity(new CustomClassEntityProvider(MarkerEntity.class));
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onClick(PlayerInteractEvent event) {
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            || event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            var player = event.getPlayer();
            var currentTick = Server.getInstance().getTick();
            if (event.getItem() instanceof ReplayNKItem item) {
                if (!PLAYER_ACTION_TIMER.containsKey(player) || currentTick - PLAYER_ACTION_TIMER.get(player) > PLAYER_ACTION_COOL_DOWN) {
                    PLAYER_ACTION_TIMER.put(player, currentTick);
                    item.onInteract(player);
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onClickEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            var currentTick = Server.getInstance().getTick();
            if (player.getInventory().getItemInHand() instanceof ReplayNKItem item) {
                if (!PLAYER_ACTION_TIMER.containsKey(player) || currentTick - PLAYER_ACTION_TIMER.get(player) > PLAYER_ACTION_COOL_DOWN) {
                    PLAYER_ACTION_TIMER.put(player, currentTick);
                    item.onClickEntity(player, event.getEntity());
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (Trail.isOperatingTrail(player)) {
            Trail.getOperatingTrail(player).stopOperating();
        }
    }
}
