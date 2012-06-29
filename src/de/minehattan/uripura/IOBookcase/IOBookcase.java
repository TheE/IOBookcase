package de.minehattan.uripura.IOBookcase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IOBookcase extends JavaPlugin {

	private Logger log = Logger.getLogger("minecraft");
	private IOBookcaseBlockListener blockListener = new IOBookcaseBlockListener(this);
	private IOBookcasePlayerListener playerListener = new IOBookcasePlayerListener(this);
	private File pluginFolder;
	private File configFile;

	public void onEnable() {

		PluginManager pm = this.getServer().getPluginManager();
		/*
		 * pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
		 * Priority.Monitor, this); pm.registerEvent(Event.Type.BLOCK_BREAK,
		 * blockListener, Priority.Highest, this);
		 * pm.registerEvent(Event.Type.BLOCK_BURN, blockListener,
		 * Priority.Monitor, this); pm.registerEvent(Event.Type.SIGN_CHANGE,
		 * blockListener, Priority.Monitor, this);
		 */
		pm.registerEvents(this.blockListener, this);
		pm.registerEvents(this.playerListener, this);

		pluginFolder = getDataFolder();
		configFile = new File(pluginFolder, "config.yml");
		createConfig();
		saveConfig();

		createFile();
		createDatabase();
		createBookFile();

		this.logMessage("Enabeld");
	}

	public void onDisable() {
		this.logMessage("Disabeld");
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("iobookcase")) {
			if (sender instanceof Player
					&& !((Player) sender).hasPermission("iobookcase.reload"))
				return true;

			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				reloadConfig();
				sender.sendMessage(ChatColor.YELLOW
						+ "IOBookcase configuration file reloaded.");
			} else
				sender.sendMessage(ChatColor.YELLOW
						+ "Use '/iobookcase reload' to reload the configuration file.");
		}

		return true;
	}

	public void logMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.info("[" + pdFile.getName() + " " + pdFile.getVersion() + "] " + msg);
	}

	public void warnMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.warning("[" + pdFile.getName() + " " + pdFile.getVersion() + "] " + msg);
	}

	private void createConfig() {
		if(!pluginFolder.exists()) {
			try {
				pluginFolder.mkdir();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				getConfig().options().copyDefaults(true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createDatabase() {
		if (!checkDatabase()) {
			this.logMessage("bookcase.db does not exist. Creating...");
			IOBookcaseDatabase database = new IOBookcaseDatabase();
			if (database.createDatabase()) {
				this.logMessage("Database created!");
			} else
				this.logMessage("Cannot create Database!");
		}
	}

	private void createFile() {
		if (!checkFile()) {
			File importFile = new File(getDataFolder() + File.separator
					+ "import.xml");
			new File(getDataFolder().toString()).mkdir();
			this.logMessage("import.xml does not exist. Creating...");
			try {
				importFile.createNewFile();
				this.logMessage("File created!");
			} catch (IOException e) {
				System.out.println("Cannot create File " + importFile.getPath()
						+ File.separator + "import.xml");
			}
		}
	}

	private void createBookFile() {
		if (!checkBookFile()) {
			File bookFile = new File(getDataFolder() + File.separator
					+ "books.txt");
			this.logMessage("books.txt does not exist. Creating...");

			InputStream input = this.getResource("books.txt");
			if (input != null) {
				FileOutputStream output = null;
				try {
					output = new FileOutputStream(bookFile);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}
					this.logMessage("books.txt created!");
				} catch (IOException e) {
					e.printStackTrace();
					System.out
							.println("Cannot create File " + bookFile.getPath()
									+ File.separator + "books.txt");
				} finally {
					try {
						input.close();
					} catch (IOException e) {
					}
					try {
						if (output != null)
							output.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	private boolean checkDatabase() {
		File importcase = new File(getDataFolder() + File.separator + "bookcase.db");
		if (importcase.exists())
			return true;
		else
			return false;
	}

	private boolean checkFile() {
		File importcase = new File(getDataFolder() + File.separator + "import.xml");
		if (importcase.exists())
			return true;
		else
			return false;
	}
	
	private boolean checkBookFile() {
		File importcase = new File(getDataFolder() + File.separator + "books.txt");
		if (importcase.exists())
			return true;
		else
			return false;
	}
}
