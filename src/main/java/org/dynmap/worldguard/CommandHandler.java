package org.dynmap.worldguard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (command.getName().equalsIgnoreCase("dynmapw")) {
			if (args.length == 0) 
				sender.sendMessage("Arguments missing.");

			else {
				if (args[0].equalsIgnoreCase("enable")) 
					new Enable(sender, args);
				else if (args[0].equalsIgnoreCase("disable")) 
					new Disable(sender, args);
				else if (args[0].equalsIgnoreCase("flag")) 
					new Flag(sender,args);
			}
		}
		return false;
	}
}
