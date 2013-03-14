package org.dynmap.worldguard;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DynmapWorldGuardPlugin extends JavaPlugin {
	private static final Logger log = Logger.getLogger("Minecraft");
	private static final String LOG_PREFIX = "[Dynmap-WorldGuard] ";
	private static final String DEF_INFOWINDOW = "<div class=\"infowindow\"><span style=\"font-size:120%;\">%regionname%</span><br /> Owner <span style=\"font-weight:bold;\">%playerowners%</span><br />Flags<br /><span style=\"font-weight:bold;\">%flags%</span></div>";
	private static DynmapWorldGuardPlugin dynmapw = null;

	Plugin dynmap;
	DynmapAPI api;
	MarkerAPI markerapi;
	WorldGuardPlugin wg;

	FileConfiguration cfg;
	MarkerSet set;
	long updperiod;
	boolean use3d;
	String infowindow;
	AreaStyle defstyle;
	Map<String, AreaStyle> cusstyle;
	Map<String, AreaStyle> cuswildstyle;
	Map<String, AreaStyle> ownerstyle;
	Map<String, AreaStyle> flagstyle;
	Set<String> visible;
	Set<String> hidden;
	Set<String> flagsToShow;
	boolean stop; 
	public boolean override = true;
	public boolean showAll = false;
	int maxdepth;

	private static class AreaStyle {
		String strokecolor;
		String unownedstrokecolor;
		double strokeopacity;
		int strokeweight;
		String fillcolor;
		double fillopacity;
		String label;

		AreaStyle(FileConfiguration cfg, String path, AreaStyle def) {
			strokecolor = cfg.getString(path+".strokeColor", def.strokecolor);
			unownedstrokecolor = cfg.getString(path+".unownedStrokeColor", def.unownedstrokecolor);
			strokeopacity = cfg.getDouble(path+".strokeOpacity", def.strokeopacity);
			strokeweight = cfg.getInt(path+".strokeWeight", def.strokeweight);
			fillcolor = cfg.getString(path+".fillColor", def.fillcolor);
			fillopacity = cfg.getDouble(path+".fillOpacity", def.fillopacity);
			label = cfg.getString(path+".label", null);
		}

		AreaStyle(FileConfiguration cfg, String path) {
			strokecolor = cfg.getString(path+".strokeColor", "#FF0000");
			unownedstrokecolor = cfg.getString(path+".unownedStrokeColor", "#00FF00");
			strokeopacity = cfg.getDouble(path+".strokeOpacity", 0.8);
			strokeweight = cfg.getInt(path+".strokeWeight", 3);
			fillcolor = cfg.getString(path+".fillColor", "#FF0000");
			fillopacity = cfg.getDouble(path+".fillOpacity", 0.35);
		}
	}

	public static void info(String msg) {
		log.log(Level.INFO, LOG_PREFIX + msg);
	}
	public static void severe(String msg) {
		log.log(Level.SEVERE, LOG_PREFIX + msg);
	}

	private class WorldGuardUpdate implements Runnable {
		public void run() {
			if(!stop)
				updateRegions();
		}
	}

	private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();

	private String formatInfoWindow(ProtectedRegion region, AreaMarker m) {
		String v = "<div class=\"regioninfo\">"+infowindow+"</div>";
		v = v.replace("%regionname%", m.getLabel());
		v = v.replace("%playerowners%", region.getOwners().toPlayersString());
		v = v.replace("%groupowners%", region.getOwners().toGroupsString());
		v = v.replace("%playermembers%", region.getMembers().toPlayersString());
		v = v.replace("%groupmembers%", region.getMembers().toGroupsString());
		if(region.getParent() != null)
			v = v.replace("%parent%", region.getParent().getId());
		else
			v = v.replace("%parent%", "");
		v = v.replace("%priority%", String.valueOf(region.getPriority()));
		Map<Flag<?>, Object> map = region.getFlags();
		String flgs = "";
		for(Flag<?> f : map.keySet()) {
			flgs += f.getName() + ": " + map.get(f).toString() + "<br/>";
		}
		v = v.replace("%flags%", flgs);
		return v;
	}

	private boolean showRegion(ProtectedRegion region) {
		if (showAll) 
			return true;
		else if (override || !region.hasMembersOrOwners()) 
			return false;

		return true;
	}

	private boolean isVisible(String id, String worldname) {
		if((visible != null) && (visible.size() > 0)) {
			if((visible.contains(id) == false) && (visible.contains("world:" + worldname) == false) &&
					(visible.contains(worldname + "/" + id) == false)) {
				return false;
			}
		}
		if((hidden != null) && (hidden.size() > 0)) {
			if(hidden.contains(id) || hidden.contains("world:" + worldname) || hidden.contains(worldname + "/" + id))
				return false;
		}
		return true;
	}

	private void addStyle(String resid, String worldid, AreaMarker m, ProtectedRegion region) {
		AreaStyle as = cusstyle.get(worldid + "/" + resid);
		if(as == null) {
			as = cusstyle.get(resid);
		}
		if(as == null) {    /* Check for wildcard style matches */
			for(String wc : cuswildstyle.keySet()) {
				String[] tok = wc.split("\\|");
				if((tok.length == 1) && resid.startsWith(tok[0]))
					as = cuswildstyle.get(wc);
				else if((tok.length >= 2) && resid.startsWith(tok[0]) && resid.endsWith(tok[1]))
					as = cuswildstyle.get(wc);
			}
		}
		if(as == null) {    /* Check for owner style matches */
			if(ownerstyle.isEmpty() != true) {
				DefaultDomain dd = region.getOwners();
				Set<String> play = dd.getPlayers();
				if(play != null) {
					for(String p : play) {
						if(as == null) {
							as = ownerstyle.get(p.toLowerCase());
						}
					}
				}
				Set<String> grp = dd.getGroups();
				if(grp != null) {
					for(String p : grp) {
						if(as == null)
							as = ownerstyle.get(p.toLowerCase());
					}
				}
			}
		}
		if(as == null) {    /* Check for flag style matches */
			Map<Flag<?>, Object> map = region.getFlags();
			for(Flag<?> f : map.keySet()) {
				as = flagstyle.get(f.getName());
			}
		}
		if(as == null)
			as = defstyle;

		boolean unowned = false;
		if((region.getOwners().getPlayers().size() == 0) &&
				(region.getOwners().getGroups().size() == 0)) {
			unowned = true;
		}
		int sc = 0xFF0000;
		int fc = 0xFF0000;
		try {
			if(unowned)
				sc = Integer.parseInt(as.unownedstrokecolor.substring(1), 16);
			else
				sc = Integer.parseInt(as.strokecolor.substring(1), 16);
			fc = Integer.parseInt(as.fillcolor.substring(1), 16);
		} catch (NumberFormatException nfx) {
		}
		m.setLineStyle(as.strokeweight, as.strokeopacity, sc);
		m.setFillStyle(as.fillopacity, fc);
		if(as.label != null) {
			m.setLabel(as.label);
		}
	}

	/* Handle specific region */
	private void handleRegion(World world, ProtectedRegion region, Map<String, AreaMarker> newmap) {
		String name = region.getId();
		double[] x = null;
		double[] z = null;


		// Only visualize Regions if enabled (override = true) and region has members or owners
		if (showRegion(region)) {

			/* Handle areas */
			if(isVisible(region.getId(), world.getName())) {
				String id = region.getId();
				String tn = region.getTypeName();
				BlockVector l0 = region.getMinimumPoint();
				BlockVector l1 = region.getMaximumPoint();

				if(tn.equalsIgnoreCase("cuboid")) { /* Cubiod region? */
					/* Make outline */
					x = new double[4];
					z = new double[4];
					x[0] = l0.getX(); z[0] = l0.getZ();
					x[1] = l0.getX(); z[1] = l1.getZ()+1.0;
					x[2] = l1.getX() + 1.0; z[2] = l1.getZ()+1.0;
					x[3] = l1.getX() + 1.0; z[3] = l0.getZ();
				}
				else if(tn.equalsIgnoreCase("polygon")) {
					ProtectedPolygonalRegion ppr = (ProtectedPolygonalRegion)region;
					List<BlockVector2D> points = ppr.getPoints();
					x = new double[points.size()];
					z = new double[points.size()];
					for(int i = 0; i < points.size(); i++) {
						BlockVector2D pt = points.get(i);
						x[i] = pt.getX(); z[i] = pt.getZ();
					}
				}
				else {  /* Unsupported type */
					return;
				}
				String markerid = world.getName() + "_" + id;
				AreaMarker m = resareas.remove(markerid); /* Existing area? */
				if(m == null) {
					m = set.createAreaMarker(markerid, name, false, world.getName(), x, z, false);
					if(m == null)
						return;
				}
				else {
					m.setCornerLocations(x, z); /* Replace corner locations */
					m.setLabel(name);   /* Update label */
				}
				if(use3d) { /* If 3D? */
					m.setRangeY(l1.getY()+1.0, l0.getY());
				}            
				/* Set line and fill properties */
				addStyle(id, world.getName(), m, region);

				/* Build popup */
				String desc = formatInfoWindow(region, m);

				m.setDescription(desc); /* Set popup */

				/* Add to map */
				newmap.put(markerid, m);
			}
		}
	}

	/* Update worldguard region information */
	public void updateRegions() {
		Map<String,AreaMarker> newmap = new HashMap<String,AreaMarker>(); /* Build new map */

		/* Loop through worlds */
		for(World w : getServer().getWorlds()) {
			RegionManager rm = wg.getRegionManager(w); /* Get region manager for world */
			if(rm == null) continue;

			Map<String,ProtectedRegion> regions = rm.getRegions();  /* Get all the regions */
			for(ProtectedRegion pr : regions.values()) {
				int depth = 1;
				ProtectedRegion p = pr;
				while(p.getParent() != null) {
					depth++;
					p = p.getParent();
				}
				if(depth > maxdepth)
					continue;
				handleRegion(w, pr, newmap);
			}
		}
		/* Now, review old map - anything left is gone */
		for(AreaMarker oldm : resareas.values()) {
			oldm.deleteMarker();
		}
		/* And replace with new map */
		resareas = newmap;

		getServer().getScheduler().scheduleSyncDelayedTask(this, new WorldGuardUpdate(), updperiod);

	}

	private class OurServerListener implements Listener {
		@SuppressWarnings("unused")
		@EventHandler(priority=EventPriority.MONITOR)
		public void onPluginEnable(PluginEnableEvent event) {
			Plugin p = event.getPlugin();
			String name = p.getDescription().getName();
			if(name.equals("dynmap") || name.equals("WorldGuard")) {
				if(dynmap.isEnabled() && wg.isEnabled())
					activate();
			}
		}
	}

	public void onEnable() {
		info("initializing");
		dynmapw = this;
		getCommand("dynmapw").setExecutor(new CommandHandler());
		PluginManager pm = getServer().getPluginManager();
		/* Get dynmap */
		dynmap = pm.getPlugin("dynmap");
		if(dynmap == null) {
			severe("Cannot find dynmap!");
			return;
		}
		api = (DynmapAPI)dynmap; /* Get API */
		/* Get WorldGuard */
		Plugin p = pm.getPlugin("WorldGuard");
		if(p == null) {
			severe("Cannot find WorldGuard!");
			return;
		}
		wg = (WorldGuardPlugin)p;

		getServer().getPluginManager().registerEvents(new OurServerListener(), this);        
		/* If both enabled, activate */
		if(dynmap.isEnabled() && wg.isEnabled())
			activate();
		/* Start up metrics */
		try {
			MetricsLite ml = new MetricsLite(this);
			ml.start();
		} catch (IOException iox) {

		}
	}

	public static DynmapWorldGuardPlugin getInstance() {
		return dynmapw;
	}

	private boolean reload = false;

	private void activate() {
		/* Now, get markers API */
		markerapi = api.getMarkerAPI();
		if(markerapi == null) {
			severe("Error loading dynmap marker API!");
			return;
		}
		/* Load configuration */
		if(reload) {
			this.reloadConfig();
		}
		else {
			reload = true;
		}
		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true);   /* Load defaults, if needed */
		this.saveConfig();  /* Save updates, if needed */

		/* Now, add marker set for mobs (make it transient) */
		set = markerapi.getMarkerSet("worldguard.markerset");
		if(set == null)
			set = markerapi.createMarkerSet("worldguard.markerset", cfg.getString("layer.name", "WorldGuard"), null, false);
		else
			set.setMarkerSetLabel(cfg.getString("layer.name", "WorldGuard"));
		if(set == null) {
			severe("Error creating marker set");
			return;
		}
		int minzoom = cfg.getInt("layer.minzoom", 0);
		if(minzoom > 0)
			set.setMinZoom(minzoom);
		set.setLayerPriority(cfg.getInt("layer.layerprio", 10));
		set.setHideByDefault(cfg.getBoolean("layer.hidebydefault", false));
		use3d = cfg.getBoolean("use3dregions", false);
		infowindow = cfg.getString("infowindow", DEF_INFOWINDOW);
		maxdepth = cfg.getInt("maxdepth", 16);

		/* Get style information */
		defstyle = new AreaStyle(cfg, "regionstyle");
		cusstyle = new HashMap<String, AreaStyle>();
		ownerstyle = new HashMap<String, AreaStyle>();
		flagstyle = new HashMap<String, AreaStyle>();
		cuswildstyle = new HashMap<String, AreaStyle>();
		ConfigurationSection sect = cfg.getConfigurationSection("custstyle");
		if(sect != null) {
			Set<String> ids = sect.getKeys(false);

			for(String id : ids) {
				if(id.indexOf('|') >= 0)
					cuswildstyle.put(id, new AreaStyle(cfg, "custstyle." + id, defstyle));
				else
					cusstyle.put(id, new AreaStyle(cfg, "custstyle." + id, defstyle));
			}
		}
		sect = cfg.getConfigurationSection("ownerstyle");
		if(sect != null) {
			Set<String> ids = sect.getKeys(false);

			for(String id : ids) {
				ownerstyle.put(id.toLowerCase(), new AreaStyle(cfg, "ownerstyle." + id, defstyle));
			}
		}
		sect = cfg.getConfigurationSection("flagstyle");
		if(sect != null) {
			Set<String> ids = sect.getKeys(false);

			for(String id : ids) {
				flagstyle.put(id.toLowerCase(), new AreaStyle(cfg, "flagstyle." + id, defstyle));
			}
		}
		List<String> vis = cfg.getStringList("visibleregions");
		if(vis != null) {
			visible = new HashSet<String>(vis);
		}
		List<String> hid = cfg.getStringList("hiddenregions");
		if(hid != null) {
			hidden = new HashSet<String>(hid);
		}
		List<String> flag = cfg.getStringList("flags");
		if(flag != null) {
			flagsToShow = new HashSet<String>(flag);
		}

		/* Set up update job - based on periond */
		int per = cfg.getInt("update.period", 300);
		if(per < 15) per = 15;
		updperiod = (long)(per*20);
		stop = false;

		getServer().getScheduler().scheduleSyncDelayedTask(this, new WorldGuardUpdate(), 40);   /* First time is 2 seconds */

		info("version " + this.getDescription().getVersion() + " is activated");
	}

	public void onDisable() {
		if(set != null) {
			set.deleteMarkerSet();
			set = null;
		}
		resareas.clear();
		stop = true;
	}

}
