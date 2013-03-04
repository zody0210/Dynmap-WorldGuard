package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;

public class Disable {

	public DynmapWorldGuardPlugin plugin = null;
	
	public Disable (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		//debug
		player.sendMessage("Disabling");
		plugin.overide = true;
	}
}
