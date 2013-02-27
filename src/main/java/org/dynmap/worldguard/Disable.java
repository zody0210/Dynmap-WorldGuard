package org.dynmap.worldguard;

import org.bukkit.command.CommandSender;

public class Disable {

	public DynmapWorldGuardPlugin plugin = null;
	
	public Disable (CommandSender sender, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		plugin.onDisable();
	}
}
