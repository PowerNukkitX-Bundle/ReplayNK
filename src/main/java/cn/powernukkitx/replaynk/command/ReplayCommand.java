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
        commandParameters.put("list", new CommandParameter[]{
                CommandParameter.newEnum("list", new String[]{"list"}),
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
                if (trail != null)
                    player.sendMessage("§aTrail " + trailName + " created!");
                else
                    player.sendMessage("§cTrail " + trailName + " already exists!");
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
            default -> {
                return 0;
            }
        }
    }
}
