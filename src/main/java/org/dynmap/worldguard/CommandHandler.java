package org.dynmap.worldguard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.worldguard.command.Disable;
import org.dynmap.worldguard.command.Enable;
import org.dynmap.worldguard.command.ShowAll;

public class CommandHandler implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;

			if (command.getName().equalsIgnoreCase("dynmapw")) {
				if (args.length == 0) 
					player.sendMessage("Arguments missing.");
				else if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("showAll")) {
					// Enable Command
					if (args[0].equalsIgnoreCase("enable")) 
						new Enable(player, args);

					// Disable Command
					if (args[0].equalsIgnoreCase("disable")) 
						new Disable(player, args);

					// ShowAll Command
					if (args[0].equalsIgnoreCase("showAll")) 
						new ShowAll(player,args);
				} else
					player.sendMessage("Unknown Command: " + args[0]);
			}
		}
		return false;
	}
}
