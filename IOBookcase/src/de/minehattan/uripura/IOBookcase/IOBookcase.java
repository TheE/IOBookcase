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

	private Logger log = Logger.getLogger( "minecraft");
	private IOBookcaseBlockListener blockListener = new IOBookcaseBlockListener( this);
	private IOBookcasePlayerListener playerListener = new IOBookcasePlayerListener( this);
	private File pluginFolder;
	private File configFile;

	public void onEnable() {
		PluginDescriptionFile pdFile = this.getDescription();
		PluginManager pm = this.getServer().getPluginManager();

		pm.registerEvents( this.blockListener, this);
		pm.registerEvents( this.playerListener, this);

		pluginFolder = getDataFolder();
		
		// create the folder if it doesn't exist
		createFolder();
		
		// Set the name for the configfile
		configFile = new File( pluginFolder, "config.yml");
		
		// create it
		createConfig();
		// ...and save
		saveConfig();

		createImportFile();
		createDatabase();
		createBookFile();

		this.logMessage( "Version " + pdFile.getVersion() + " Enabeld");
	}

	public void onDisable() {
		PluginDescriptionFile pdFile = this.getDescription();
		
		this.logMessage( "Version " + pdFile.getVersion() + " Disabeld");
	}

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
		if( command.getName().equalsIgnoreCase( "iobookcase")) {
			if( sender instanceof Player && !( ( Player) sender).hasPermission( "iobookcase.reload"))
				return true;

			if( args.length > 0 && args[0].equalsIgnoreCase( "reload")) {
				reloadConfig();
				sender.sendMessage( ChatColor.YELLOW + "IOBookcase configuration file reloaded.");
			} else
				sender.sendMessage( ChatColor.YELLOW + "Use '/iobookcase reload' to reload the configuration file.");
		}
		return true;
	}

	public void logMessage( String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.info( "[" + pdFile.getName() + "] " + msg);
	}

	public void warnMessage( String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.warning( "[" + pdFile.getName() + "] " + msg);
	}
	
	public void errorMessage( String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		this.log.severe( "[" + pdFile.getName() + "] " + msg);
	}

	private void createFolder() {
		if( !pluginFolder.exists()) {
			try {
				pluginFolder.mkdir();
			} catch( Exception e) {
				this.errorMessage( "Cannot create main folder");
				e.printStackTrace();
			}
		}
	}
	
	private void createConfig() {
		if( !configFile.exists()) {
			try {
				getConfig().options().copyDefaults( true);
			} catch( Exception e) {
				this.errorMessage( "Cannot create config");
				e.printStackTrace();	
			}
		}
	}

	private void createDatabase() {
		if( !checkFile( "bookcase.db")) {
			this.logMessage( "bookcase.db does not exist. Creating...");
			IOBookcaseDatabase database = new IOBookcaseDatabase();
			if( database.createDatabase()) {
				this.logMessage( "Database created!");
			} else
				this.errorMessage( "Cannot create Database!");
		}
	}

	private void createImportFile() {
		if( !checkFile( "import.xml")) {
			File importFile = new File( getDataFolder() + File.separator + "import.xml");

			this.logMessage( "import.xml does not exist. Creating...");
			try {
				importFile.createNewFile();
				this.logMessage( "File created!");
			} catch( IOException e) {
				this.errorMessage( "Cannot create File " + importFile.getPath() + File.separator + "import.xml");
			}
		}
	}

	private void createBookFile() {
		if( !checkFile( "books.txt")) {
			File bookFile = new File( getDataFolder() + File.separator + "books.txt");
			this.logMessage( "books.txt does not exist. Creating...");

			InputStream input = this.getResource( "books.txt");
			if( input != null) {
				FileOutputStream output = null;
				try {
					output = new FileOutputStream( bookFile);
					byte[] buf = new byte[8192];
					int length = 0;
					while( ( length = input.read( buf)) > 0) {
						output.write( buf, 0, length);
					}
					this.logMessage( "books.txt created!");
				} catch( IOException e) {
					this.errorMessage( "Cannot create File " + bookFile.getPath() + File.separator + "books.txt");
					e.printStackTrace();
					
				} finally {
					try {
						input.close();
					} catch( IOException e) {
					}
					try {
						if( output != null)
							output.close();
					} catch( IOException e) {
					}
				}
			}
		}
	}

	private boolean checkFile( String file) {
		File testfile = new File( getDataFolder() + File.separator + file);
		if( testfile.exists())
			return true;
		else
			return false;
	}
}
