package de.minehattan.uripura.IOBookcase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

	public IOBookcasePlayerListener(IOBookcase instance) {
		this.plugin = instance;
	}

	protected String getBookLine() throws IOException {
		RandomAccessFile file = new RandomAccessFile(new File(
				plugin.getDataFolder(), "books.txt"), "r");

		long len = file.length();
		byte[] data = new byte[500];

		for (int tries = 0; tries < 3; tries++) {
			int j = rand.nextInt((int) len);
			if (tries == 2) { // File is too small
				j = 0;
			}
			file.seek(j);
			file.read(data);

			StringBuilder buffer = new StringBuilder();
			boolean found = j == 0;
			byte last = 0;

			for (int i = 0; i < data.length; i++) {
				if (found) {
					if (data[i] == 10 || data[i] == 13 || i >= len) {
						if (last != 10 && last != 13) {
							file.close();
							return buffer.toString();
						}
					} else {
						buffer.appendCodePoint(data[i]);
					}
				} else if (data[i] == 10 || data[i] == 13) { // Line feeds
					found = true;
				}

				last = data[i];
			}
		}

		file.close();
		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String worldName = player.getWorld().getName();
		Block block = event.getClickedBlock();

		// Check if the player is actually interacting with a bookcase
		// Also if the player holds a block we don't want to display text
		if (event.isCancelled() || block.getType() != Material.BOOKSHELF)
			return;

		if (player.hasPermission("iobookcase.read") == false)
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();
		if (connection.checkCase(worldName, block.getX(), block.getY(),
				block.getZ())) {
			player.sendMessage(ChatColor.GRAY + IOBookcase.msgPickBook);
			connection.readCase(player, worldName, block.getX(), block.getY(),
					block.getZ());
		} else if (IOBookcase.randomText) {
			player.sendMessage(IOBookcase.msgPickBook);
			try {
				player.sendMessage(ChatColor.YELLOW + this.getBookLine());
			} catch (IOException e) {
				player.sendMessage(ChatColor.RED
						+ "Failed to fetch a line from the books file.");
			}
		} else
			player.sendMessage(ChatColor.YELLOW + IOBookcase.msgEmptyBookcase);
	}
}
