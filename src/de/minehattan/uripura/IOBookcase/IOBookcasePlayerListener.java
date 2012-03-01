package de.minehattan.uripura.IOBookcase;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.minehattan.uripura.IOBookcase.IOBookcase;
import de.minehattan.uripura.IOBookcase.IOBookcaseDatabase;

public class IOBookcasePlayerListener implements Listener {

	// private IOBookcaseReborn plugin;

	public IOBookcasePlayerListener(IOBookcase instance) {
		// plugin = instance;
	}

	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String worldName = player.getWorld().getName();
		Block block;

		// Check if the player is actually interacting with a block
		if(event.getClickedBlock() == null)
			return;
		block = event.getClickedBlock();
			
		if (block.getType() != Material.BOOKSHELF)
			return;
		
		if (player.hasPermission("iobookcasereborn.read") == false)
			return;

		if ((player.getItemInHand().getType().isBlock()) == false || (player.getItemInHand().getType() == Material.AIR)
				&& player.getItemInHand().getType() != Material.SIGN) {
			
			IOBookcaseDatabase connection = new IOBookcaseDatabase();
			if(connection.checkCase(worldName, block.getX(), block.getY(), block.getZ())) {
				connection.readCase(player, worldName, block.getX(), block.getY(), block.getZ());
			}
			else
				player.sendMessage(ChatColor.RED + "This bookcase is empty.");
		}
	}
}
