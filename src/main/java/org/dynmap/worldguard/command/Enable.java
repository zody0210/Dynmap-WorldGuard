package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;
import org.dynmap.worldguard.permissions.PermissionChecker;

public class Enable {

	public DynmapWorldGuardPlugin plugin = null;
	PermissionChecker permcheck = new PermissionChecker();

	public Enable (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		if (permcheck.hasEnablePerm(player)) {
			player.sendMessage("Enabling");
			plugin.override = false;
		} else
			player.sendMessage("No permission");

	}
}
