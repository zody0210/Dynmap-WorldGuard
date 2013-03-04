package org.dynmap.worldguard.permissions;

import org.bukkit.entity.Player;

public class PermissionChecker {

	public boolean hasEnablePerm(Player player) {
		if (player.hasPermission("dynmapw.enable") || player.hasPermission("dynmapw.*") || player.isOp())
			return true;
		return false;
	}
	
	public boolean hasDisablePerm(Player player) {
		if (player.hasPermission("dynmapw.disable") || player.hasPermission("dynmapw.*") || player.isOp())
			return true;
		return false;
	}
	
	public boolean hasShowAll(Player player) {
		if (player.hasPermission("dynmapw.showall") || player.hasPermission("dynmapw.*") || player.isOp())
			return true;
		return false;
	}


}
