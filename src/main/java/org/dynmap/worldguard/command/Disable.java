package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;
import org.dynmap.worldguard.permissions.PermissionChecker;

public class Disable {

	public DynmapWorldGuardPlugin plugin = null;
	PermissionChecker permcheck = new PermissionChecker();

	public Disable (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		if (permcheck.hasDisablePerm(player)) {
			player.sendMessage("Disabling");
			plugin.override = true;
		} else
			player.sendMessage("No permission");

	}
}
