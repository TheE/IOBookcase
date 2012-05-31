package de.minehattan.uripura.IOBookcase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

public class IOBookcaseConfig {

	private YamlConfiguration config;
	private HashMap<String, Object> configDefaults = new HashMap<String, Object>();

	public IOBookcaseConfig(File cFile) {
		this.config = new YamlConfiguration();

		this.configDefaults.put("sqlite-database-name", "bookcase.db");
		this.configDefaults.put("drop-bookcase", true);
		this.configDefaults.put("random-text", true);
		this.configDefaults.put("msg-empty-bookcase", "This bookcase is empty.");
		this.configDefaults.put("msg-pick-book", "You pick up a book...");
		this.configDefaults.put("msg-notify-written", "Text written to line");
		this.configDefaults.put("msg-notify-deleted", "Bookcase unregistered.");
		this.configDefaults.put("msg-notify-found", "Bookcase was imported.");
		this.configDefaults.put("msg-error-lines", "Only lines 1-10 allowed.");
		this.configDefaults.put("msg-error-format", "The format is @line linenumber [color]");
		this.configDefaults.put("msg-error-exeption", "Failed to write:");
		this.configDefaults.put("msg-error-color", "You selected a none valid color: Defaulting to white");
		this.configDefaults.put("msg-error-import", "Could not find the case's name, make sure your format is correct");
		this.configDefaults.put("msg-error-to-few-arguments", "To few Arguments. Try again");
		this.configDefaults.put("msg-error-find-file", "Unable to find the file. Contact you Server administartor!");
		
		if (cFile.exists() == false) {
			for (String key : this.configDefaults.keySet())
				this.config.set(key, this.configDefaults.get(key));
			try {
				this.config.save(cFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.config.load(cFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int getInt(String key) {
		if (this.configDefaults.containsKey(key) == false)
			return 0;
		return this.config.getInt(key, (Integer) this.configDefaults.get(key));
	}

	public boolean getBoolean(String key) {
		if (this.configDefaults.containsKey(key) == false)
			return false;
		return this.config.getBoolean(key, (Boolean) this.configDefaults.get(key));
	}

	public String getString(String key) {
		if (this.configDefaults.containsKey(key) == false)
			return "";
		return this.config.getString(key, (String) this.configDefaults.get(key));
	}
}
