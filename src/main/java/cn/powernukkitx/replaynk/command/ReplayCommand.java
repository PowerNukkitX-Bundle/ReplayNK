package cn.powernukkitx.replaynk.command;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import cn.powernukkitx.replaynk.ReplayNK;
import cn.powernukkitx.replaynk.trail.Trail;

import java.util.Map;

/**
 * @author daoge_cmd
 * @date 2023/6/16
 * ReplayNK Project
 */
public class ReplayCommand extends PluginCommand<ReplayNK> {
    public ReplayCommand(ReplayNK plugin) {
        super("replay", "replaynk.commands.replay.description", plugin);
        setPermission("replaynk.command.replay");
        commandParameters.clear();
        commandParameters.put("operate", new CommandParameter[]{
                CommandParameter.newEnum("operate", new String[]{"operate"}),
                CommandParameter.newType("name", false, CommandParamType.STRING)
        });
        commandParameters.put("create", new CommandParameter[]{
                CommandParameter.newEnum("create", new String[]{"create"}),
                CommandParameter.newType("name", false, CommandParamType.STRING)
        });
        commandParameters.put("remove", new CommandParameter[]{
                CommandParameter.newEnum("remove", new String[]{"remove"}),
                CommandParameter.newType("name", false, CommandParamType.STRING)
        });
        commandParameters.put("list", new CommandParameter[]{
                CommandParameter.newEnum("list", new String[]{"list"}),
        });
        commandParameters.put("showbc", new CommandParameter[]{
                CommandParameter.newEnum("showbc", new String[]{"showbc"}),
        });
        enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        if (!sender.isPlayer()) {
            sender.sendMessage("§cThis command can only be executed by player!");
            return 0;
        }
        var player = sender.asPlayer();
        switch (result.getKey()) {
            case "operate" -> {
                String trailName = result.getValue().get(1).get();
                var trail = Trail.getTrail(trailName);
                if (trail == null) {
                    sender.sendMessage("§cTrail not found!");
                    return 0;
                }
                trail.startOperating(player);
                player.sendMessage("§aTrail operating started!");
                return 1;
            }
            case "create" -> {
                String trailName = result.getValue().get(1).get();
                var trail = Trail.create(trailName);
                if (trail != null) {
                    player.sendMessage("§aTrail " + trailName + " created!");
                    trail.startOperating(player);
                    player.sendMessage("§aTrail operating started!");
                } else {
                    player.sendMessage("§cTrail " + trailName + " already exists!");
                }
                return 1;
            }
            case "remove" -> {
                String trailName = result.getValue().get(1).get();
                var trail = Trail.removeTrail(trailName);
                if (trail != null)
                    player.sendMessage("§aTrail " + trailName + " removed!");
                else
                    player.sendMessage("§cTrail " + trailName + " not exists!");
                return 1;
            }
            case "list" -> {
                var strBuilder = new StringBuilder("§aTrails: ");
                var trails = Trail.getTrails();
                for (var trail : trails.values()) {
                    strBuilder.append(trail.getName()).append(" ");
                }
                sender.sendMessage(strBuilder.toString());
                return 1;
            }
//            case "showbc" -> {
//                var trail = Trail.getOperatingTrail(player);
//                if (trail == null) {
//                    player.sendMessage("§cYou are not operating any trail!");
//                    return 0;
//                }
//                if (!trail.isUseBezierCurves()) {
//                    player.sendMessage("§cThis trail does not use bezier curves!");
//                    return 0;
//                }
//                trail.prepareRuntimeMarkers();
//                var startTime = Server.getInstance().getTick();
//                Server.getInstance().getScheduler().scheduleRepeatingTask(new Task() {
//                    @Override
//                    public void onRun(int currentTick) {
//                        trail.getRuntimeMarkers().forEach(marker -> player.getLevel().addParticleEffect(new Vector3(marker.getX(), marker.getY(), marker.getZ()), ParticleEffect.BALLOON_GAS));
//                        if (currentTick - startTime >= 200) {
//                            this.cancel();
//                        }
//                    }
//                }, 5);
//                return 1;
//            }
            default -> {
                return 0;
            }
        }
    }
}
