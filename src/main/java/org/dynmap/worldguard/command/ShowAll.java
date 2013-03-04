package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;

public class ShowAll {
	public DynmapWorldGuardPlugin plugin = null;

	public ShowAll (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		player.sendMessage("Showing all regions");
		if (args.length > 0) {
			if (args[0] == "true")
				plugin.showAll = true;
			else if (args[0] == "false")
				plugin.showAll = false;

		} else
			player.sendMessage("Missing second argument");			
	}
}