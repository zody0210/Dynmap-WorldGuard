package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;

public class Enable {

	public DynmapWorldGuardPlugin plugin = null;
	
	public Enable (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		player.sendMessage("Enabling");
		plugin.overide = false;
		
	}
}
