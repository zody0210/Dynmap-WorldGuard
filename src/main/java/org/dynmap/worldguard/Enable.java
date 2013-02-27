package org.dynmap.worldguard;

import org.bukkit.command.CommandSender;

public class Enable {

	public DynmapWorldGuardPlugin plugin = null;
	
	public Enable (CommandSender sender, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		plugin.stop = false;
		
	}
}
