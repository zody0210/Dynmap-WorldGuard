package org.dynmap.worldguard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.worldguard.command.Disable;
import org.dynmap.worldguard.command.Enable;
import org.dynmap.worldguard.command.ShowAll;
import org.dynmap.worldguard.permissions.PermissionChecker;

public class CommandHandler implements CommandExecutor {

	PermissionChecker permcheck = new PermissionChecker();

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;

			if (command.getName().equalsIgnoreCase("dynmapw")) {
				if (args.length == 0) 
					player.sendMessage("Arguments missing.");
				else if (commands.valueOf(args[0]) != commands.enable || commands.valueOf(args[0]) != commands.disable || commands.valueOf(args[0]) != commands.showAll )
					player.sendMessage("Unknown command");

				else {
					// Enable Command
					if (args[0].equalsIgnoreCase("enable") && permcheck.hasEnablePerm(player)) 
						new Enable(player, args);
					else
						player.sendMessage("No Permission to enable");
					
					// Disable Command
					if (args[0].equalsIgnoreCase("disable") && permcheck.hasDisablePerm(player)) 
						new Disable(player, args);
					else
						player.sendMessage("No Permission to disable");
					
					// ShowAll Command
					if (args[0].equalsIgnoreCase("showAll") && permcheck.hasShowAll(player)) 
						new ShowAll(player,args);
					else
						player.sendMessage("No Permission to showAll");
				}
			}
		}
		return false;
	}
	
	public enum commands {
		enable, disable, showAll;
	}
}
