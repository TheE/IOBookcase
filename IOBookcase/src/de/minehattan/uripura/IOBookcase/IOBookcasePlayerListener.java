package de.minehattan.uripura.IOBookcase;

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

	public IOBookcasePlayerListener(IOBookcase instance) {
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
		if (connection.checkCase(worldName, block.getX(), block.getY(), block.getZ())) {
			connection.readCase(player, worldName, block.getX(), block.getY(), block.getZ());
		} else
			player.sendMessage(ChatColor.RED + "This bookcase is empty.");
	}
}
