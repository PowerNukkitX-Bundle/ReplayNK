package cn.powernukkitx.replaynk.command;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
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
        super("replay", "replaynk.command.replay.description", plugin);
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
        enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        if (!sender.isPlayer()) {
            log.addMessage("replaynk.command.replay.onlyplayer").output();
            return 0;
        }
        var player = sender.asPlayer();
        switch (result.getKey()) {
            case "operate" -> {
                if (Trail.isOperatingTrail(player)) {
                    log.addMessage("replaynk.trail.alreadyoperatingtrail").output();
                    return 0;
                }
                String trailName = result.getValue().get(1).get();
                var trail = Trail.getTrail(trailName);
                if (trail == null) {
                    log.addMessage("replaynk.trail.notfound", trailName).output();
                    return 0;
                }
                trail.startOperating(player);
                log.addMessage("replaynk.trail.startoperating", trailName).output();
                return 1;
            }
            case "create" -> {
                String trailName = result.getValue().get(1).get();
                var trail = Trail.create(trailName);
                if (trail != null) {
                    log.addMessage("replaynk.trail.created", trailName);
                    trail.startOperating(player);
                    log.addMessage("replaynk.trail.startoperating", trailName).output();
                } else {
                    log.addMessage("replaynk.trail.alreadyexist", trailName).output();
                }
                return 1;
            }
            case "remove" -> {
                String trailName = result.getValue().get(1).get();
                var trail = Trail.removeTrail(trailName);
                if (trail != null)
                    log.addMessage("replaynk.trail.removed", trailName).output();
                else
                    log.addMessage("replaynk.trail.notfound", trailName).output();
                return 1;
            }
            case "list" -> {
                var strBuilder = new StringBuilder();
                var trails = Trail.getTrails();
                for (var trail : trails.values()) {
                    strBuilder.append(trail.getName()).append(" ");
                }
                log.addMessage("replaynk.command.replay.list", strBuilder.toString()).output();
                return 1;
            }
            default -> {
                return 0;
            }
        }
    }
}
