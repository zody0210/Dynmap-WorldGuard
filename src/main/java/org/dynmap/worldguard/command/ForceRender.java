package org.dynmap.worldguard.command;

import org.bukkit.entity.Player;
import org.dynmap.worldguard.DynmapWorldGuardPlugin;
import org.dynmap.worldguard.permissions.PermissionChecker;

public class ForceRender {

	public DynmapWorldGuardPlugin plugin = null;
	PermissionChecker permcheck = new PermissionChecker();

	public ForceRender (Player player, String[] args) {
		this.plugin = DynmapWorldGuardPlugin.getInstance();
		if (permcheck.hasForce(player)) {
			player.sendMessage("Rendering Regions now");
			plugin.updateRegions();
		} else
			player.sendMessage("No permission");

	}
}
