package me.bomb.permissionattributes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import static me.bomb.permissionattributes.PermissionAttributes.PERMISSION_PREFIX;
import static me.bomb.permissionattributes.PermissionAttributes.DEFAULT;
import static me.bomb.permissionattributes.PermissionAttributes.PERMISSION_PREFIX_SIZE;

public final class AttributeUpdater extends Thread {
	
	private volatile boolean run;
	
	private String[] ids;
	private HashMap<String, EnumMap<Attribute, Double>> attributes;
	private final Server server;
	
	protected AttributeUpdater(final Configuration configuration, final Server server) {
		this.server = server;
		setConfiguration(configuration);
	}
	
	protected void setConfiguration(final Configuration configuration) {
		this.ids = configuration.getIds();
		this.attributes = configuration.get();
	}
	
	@Override
	public void start() {
		run = true;
		super.start();
	}
	
	public void run() {
		HashMap<Player, EnumMap<Attribute, Double>> tosetattr = new HashMap<>();
		HashMap<Player, Integer> permissionhashes = new HashMap<>();
		while (run) {
			synchronized(this) {
				for(Entry<Player, EnumMap<Attribute, Double>> tosetentry : tosetattr.entrySet()) {
					final Player player = tosetentry.getKey();
					EnumMap<Attribute, Double> attributemap = tosetentry.getValue();
					for(Entry<Attribute, Double> attributeentry : attributemap.entrySet()) {
						AttributeInstance attribute = player.getAttribute(attributeentry.getKey());
						final double value = attributeentry.getValue();
						if(attribute.getBaseValue() == value) {
							continue;
						}
						attribute.setBaseValue(value);
					}
				}
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
			tosetattr.clear();
			ArrayList<Player> players = new ArrayList<>(server.getOnlinePlayers());
			permissionhashes.keySet().retainAll(players);
			for(Player player : players) {
				final Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
				final int hashcode = permissions.hashCode();
				final Integer previoushash = permissionhashes.get(player);
				if(previoushash == null || hashcode != previoushash) {
					permissionhashes.put(player, hashcode);
					String apply = DEFAULT;
					for(PermissionAttachmentInfo permissionattachmentinfo : permissions) {
						final String permission;
						if(!permissionattachmentinfo.getValue() || !(permission = permissionattachmentinfo.getPermission()).startsWith(PERMISSION_PREFIX, 0)) {
							continue;
						}
						int i = ids.length;
						while(--i > -1) {
							String group = ids[i];
							if(permission.startsWith(group, PERMISSION_PREFIX_SIZE)) {
								apply = group;
								break;
							}
						}
					}
					EnumMap<Attribute, Double> attributemap = attributes.get(apply);
					if(attributemap == null) {
						continue;
					}
					tosetattr.put(player, attributemap);
				}
			}
		}
	}
	
	public void end() {
		run = false;
		synchronized(this) {
			this.notify();
		}
	}
}
