package de.minehattan.uripura.IOBookcase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.minehattan.uripura.IOBookcase.IOBookcase;
import de.minehattan.uripura.IOBookcase.IOBookcaseDatabase;

public class IOBookcasePlayerListener implements Listener {

	private IOBookcase plugin;
	protected static Random rand = new Random();

	public IOBookcasePlayerListener( IOBookcase instance) {
		this.plugin = instance;
	}

	protected String getBookLine() throws IOException {
		// Number of lines
		int lines;
		// counter for the lines we passed
		int passes = 0;
		String line = null;
		
		// Get the number of lines
		LineNumberReader lnr = new LineNumberReader( new FileReader( new File( plugin.getDataFolder() + File.separator, "books.txt")));
		lnr.skip( Long.MAX_VALUE);
		lines = lnr.getLineNumber();
		lnr.close();
		
		// Choose a random number that is smaller than the number of lines
		int toRead = new Random().nextInt( lines);
		BufferedReader br = new BufferedReader( new FileReader( new File( plugin.getDataFolder() + File.separator, "books.txt")));
		
		while( ( line = br.readLine()) != null) {
			passes++;
			if( passes > toRead)
				break;
		}
		br.close();
		return line;
	}

	@EventHandler( priority = EventPriority.MONITOR)
	public void onPlayerInteract( PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String worldName = player.getWorld().getName();
		Block block = event.getClickedBlock();

		// Check if the player is actually interacting with a bookcase
		// Also if the player holds a block we don't want to display text
		if( event.isCancelled() || block.getType() != Material.BOOKSHELF || player.getItemInHand().getType() != Material.AIR)
			return;

		if( player.hasPermission( "iobookcase.read") == false)
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();
		player.sendMessage( ChatColor.GRAY + plugin.getConfig().getString( "msg-pick-book"));
		
		if( connection.checkCase( worldName, block.getX(), block.getY(), block.getZ())) {
			connection.readCase( player, worldName, block.getX(), block.getY(), block.getZ());
			
		} else if( plugin.getConfig().getBoolean( "random-text")) {
			try {
				player.sendMessage( ChatColor.YELLOW + this.getBookLine());
			} catch( IOException e) {
				// Inform the serveradmin, and the player
				plugin.errorMessage( plugin.getConfig().getString( "msg-error-fetch-line"));
				player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-fetch-line"));
			}
		} else
			player.sendMessage( ChatColor.YELLOW + plugin.getConfig().getString( "msg-empty-bookcase"));
		// Close the connection
		connection.closeConnection();
	}
}
