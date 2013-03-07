package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;
import org.dynmap.worldguard.permissions.PermissionChecker;

public class ShowAll {
	public DynmapWorldGuardPlugin plugin = null;
	PermissionChecker permcheck = new PermissionChecker();

	public ShowAll (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		if (permcheck.hasShowAll(player)) {
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("on")) {
					plugin.showAll = true;
					player.sendMessage("Showing all regions");
				} else if (args[1].equalsIgnoreCase("off")) {
					plugin.showAll = false;
					player.sendMessage("Showing only regions with members");
				}

			} else
				player.sendMessage("Missing second argument");		
		} else
			player.sendMessage("No permission");
	}
}