package me.bomb.permissionattributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class PermissionAttributes extends JavaPlugin {
	
	protected final static String PERMISSION_PREFIX = "pa.", DEFAULT = "default";
	protected final static int PERMISSION_PREFIX_SIZE = PERMISSION_PREFIX.length();
	
	private final Logger logger;
	private final Server server;
	private final Path plugindir;
	private Configuration configuration;
	private AttributeUpdater attributeupdater;
	private int taskid = -1;
	
	public PermissionAttributes() {
		this.logger = this.getLogger();
		this.server = this.getServer();
		final Path plugindir = getDataFolder().toPath();
		final FileSystemProvider fsp = plugindir.getFileSystem().provider();
		try {
			fsp.createDirectory(plugindir);
		} catch (IOException e) {
		}
		this.plugindir = plugindir;
	}
	
	public void onEnable() {
		try {
			this.configuration = new Configuration(this.logger, plugindir.resolve("config.yml"));
			this.attributeupdater = new AttributeUpdater(configuration, server);
			this.attributeupdater.start();
			final BukkitScheduler scheduler = server.getScheduler();
			this.taskid = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					synchronized(attributeupdater) {
						attributeupdater.notify();
					}
				};
			}, 0L, 1L);
			logger.info("Started successfuly!");
		} catch (Exception e) {
		}
	}
	
	public void onDisable() {
		if(this.taskid != -1) {
			server.getScheduler().cancelTask(this.taskid);
		}
		if(this.attributeupdater == null) {
			return;
		}
		this.attributeupdater.end();
		this.attributeupdater = null;
	}
	
	public Configuration getConfiguration() {
		return this.configuration;
	}
	
}
