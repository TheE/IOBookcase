package de.minehattan.uripura.IOBookcase;

//import java.beans.Statement;
import java.io.File;
import java.io.IOException;
/*import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;*/
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IOBookcase extends JavaPlugin {

	private Logger log = Logger.getLogger("minecraft");
	private IOBookcaseBlockListener blockListener = new IOBookcaseBlockListener(this);
	private IOBookcasePlayerListener playerListener = new IOBookcasePlayerListener(this);
	public String pluginDirPath;
	public File configFile;
	public IOBookcaseConfig config;

	public void onEnable() {

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Monitor, this);

		this.pluginDirPath = this.getDataFolder().getAbsolutePath();
		this.configFile = new File(this.pluginDirPath + File.separator + "config.yml");
		this.config = new IOBookcaseConfig(this.configFile);

		if(!checkFile())
			createFile();
		if(!checkDatabase())
			createDatabase();

		this.logMessage("Enabeld");
	}

	public void onDisable() {
		this.logMessage("Disabeld");
	}

	public void logMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.info("[" + pdFile.getName() + " " + pdFile.getVersion() + "] " + msg);
	}

	public void warnMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.warning("[" + pdFile.getName() + " " + pdFile.getVersion() + "] " + msg);
	}

	private void createDatabase() {
		this.logMessage("bookcase.db does not exist. Creating...");
		IOBookcaseDatabase database = new IOBookcaseDatabase();
		if(database.createDatabase()) {
			this.logMessage("Database created!");
		}
		else
			this.logMessage("Cannot create Database!");
		
	}
	
	private void createFile() {

		File importFile = new File(getDataFolder() + File.separator + "import.txt");

		new File(getDataFolder().toString()).mkdir();

		this.logMessage("import.txt does not exist. Creating...");
		try {
			importFile.createNewFile();
			this.logMessage("File created!");

		} catch (IOException e) {
			System.out.println("Cannot create File " + importFile.getPath() + File.separator + "import.txt");
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
		File importcase = new File(getDataFolder() + File.separator + "import.txt");
		if (importcase.exists())
			return true;
		else
			return false;
	}
}
