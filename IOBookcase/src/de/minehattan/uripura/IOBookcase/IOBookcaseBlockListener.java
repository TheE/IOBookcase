package de.minehattan.uripura.IOBookcase;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

public class IOBookcaseBlockListener implements Listener {

	private IOBookcase plugin;

	public IOBookcaseBlockListener(IOBookcase instance) {
		this.plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		// Stores the first line of the sign
		String firstLine = null;
		// Stores the left lines of the sign
		StringBuffer bufferText = new StringBuffer();

		Player player = event.getPlayer();
		Block block = event.getBlock();
		Block sign = event.getBlock();
		boolean signOnCase = false;

		if (event.isCancelled())
			return;
		if (player.hasPermission("iobookcase.write") == false)
			return;

		// Check if the Sign was placed on one of the Sides...
		if (event.getBlock().getRelative(BlockFace.NORTH).getType() == Material.BOOKSHELF) {
			block = event.getBlock().getRelative(BlockFace.NORTH);
			signOnCase = true;
		} else if (event.getBlock().getRelative(BlockFace.EAST).getType() == Material.BOOKSHELF) {
			block = event.getBlock().getRelative(BlockFace.EAST);
			signOnCase = true;
		} else if (event.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.BOOKSHELF) {
			block = event.getBlock().getRelative(BlockFace.SOUTH);
			signOnCase = true;
		} else if (event.getBlock().getRelative(BlockFace.WEST).getType() == Material.BOOKSHELF) {
			block = event.getBlock().getRelative(BlockFace.WEST);
			signOnCase = true;
		}
		// ...if not: stop here
		if (signOnCase == false)
			return;

		// Store the first line in firstLine and the rest in bufferText
		firstLine = event.getLine(0).toLowerCase();
		for (int i = 1; i < 4; i++) {
			bufferText.append(event.getLine(i));
			// player.sendMessage("BufferText: " + bufferText.toString());
		}

		// Check if the first line contains @line or @import
		if (firstLine.contains("@line")) {
			handleLine(player, block, sign, firstLine, bufferText.toString());
		} else if (firstLine.contains("@import")) {
			handleImport(player, block, sign, firstLine, bufferText.toString());
		} else
			player.sendMessage(ChatColor.RED
					+ plugin.getConfig().getString("msg-error-format"));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = event.getPlayer().getWorld().getName();

		if (block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = event.getBlock().getX();
			int y = event.getBlock().getY();
			int z = event.getBlock().getZ();

			checkcase = connection.checkCase(worldName, x, y, z);

			if (checkcase) {
				connection.deleteCase(worldName, x, y, z);
				event.getPlayer().sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-notify-deleted"));

			}
			if (plugin.getConfig().getBoolean("drop-bookcase")) {
				// We cancel the event and drop a normal bookcase
				
				event.setCancelled(true);
				block.setType(Material.AIR);
				block.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.BOOKSHELF, 1));
			}
		} catch (Exception e) {
			System.out.println("delete fail: " + e);
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = block.getWorld().getName();
		
		if (block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;
		
		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = event.getBlock().getX();
			int y = event.getBlock().getY();
			int z = event.getBlock().getZ();

			checkcase = connection.checkCase(worldName, x, y, z);

			if (checkcase) {
				connection.deleteCase(worldName, x, y, z);
			}

		} catch (Exception e) {
			System.out.println("delete fail: " + e);
		}
	}

	private void handleLine(Player player, Block block, Block sign,
			String firstLine, String otherLines) {
		// Linenumber in the bookcase
		int lineNum = 1;
		// Color of the line (default = white)
		String lineColor = "§f";
		// String that is send to the database
		String textToWrite;
		// For splitting the first line
		String[] firstLineString;
		// Name of the current world
		String worldName = player.getWorld().getName();

		firstLineString = firstLine.split(" ");

		/*
		 * Format: @line NUMBER [COLOR_AS_STRING]
		 * If the array contains more than 1 string we probably got a line number
		 */
		if (firstLineString.length > 1) {
			
			// Check whether this string is numeric or not
			if (isIntNumber(firstLineString[1])) {
				lineNum = Integer.parseInt(firstLineString[1]);
			} else {
				player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-format"));
				return;
			}
			
			// If it's bigger than 2 we got an additional color
			if (firstLineString.length > 2) {
				firstLineString[2] = firstLineString[2].toLowerCase();
				lineColor = getColor(firstLineString[2], player);
			}
		} else {
			player.sendMessage(ChatColor.RED
					+ plugin.getConfig().getString("msg-error-to-few-arguments"));
			return;
		}

		// Create the String that is send o the Database
		textToWrite = lineColor + otherLines;

		if (lineNum < 11 && lineNum > 0) {
			/*
			 * The text is safe to be send to the database
			 */
			IOBookcaseDatabase connection = new IOBookcaseDatabase();
			connection.writeSql(textToWrite, lineNum, worldName, block.getX(),
					block.getY(), block.getZ());
			player.sendMessage(ChatColor.YELLOW
					+ plugin.getConfig().getString("msg-notify-written") + " "
					+ lineNum);

			// Delete the sign and give it back to the player
			giveSignBack(player, sign);

		} else
			player.sendMessage(ChatColor.RED
					+ plugin.getConfig().getString("msg-error-lines"));

	}

	private void handleImport(Player player, Block bookcase, Block sign, String firstLine, String otherLines) {
		// import from text file
		if (firstLine.contains("@import")) {

			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse (plugin.getDataFolder() + File.separator + "import.xml");

            // normalize text representation
            doc.getDocumentElement ().normalize ();

            NodeList cases = doc.getElementsByTagName("case");
            
            int numberOfCases = cases.getLength();
            //System.out.println("So viele Case-dinger : " + numberOfCases);
            //System.out.println ("Root element: " + doc.getDocumentElement().getNodeName());
			
            for(int i=0; i < numberOfCases; i++) {
            	
            	Node firstPersonNode = cases.item(i);
                
            	if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element firstPersonElement = (Element)firstPersonNode;
                    
                    //-------
                    NodeList caseLineList = firstPersonElement.getElementsByTagName("line");
                    
                    
                 // Attribute
                	if(firstPersonElement.hasAttributes()) {
                		NamedNodeMap attrs = firstPersonElement.getAttributes();
                		for(int u = 0 ; u<attrs.getLength() ; u++) {
                	        Attr attribute = (Attr)attrs.item(u);     
                	        //System.out.println("Attribut: " + attribute.getName() + " = "+attribute.getValue());
                	        
                	        String blah1, blah2;
                	        blah1 = attribute.getValue();
                	        blah2 = otherLines;
                	        
                	        player.sendMessage("wert: '" + blah1 + "'");
                	        player.sendMessage("otherlines: '" + blah2 + "'");
                	        
                	        if(blah1.equals(blah2)) {
                	        	player.sendMessage(ChatColor.RED + "Case wurde gefunden!");                	        	
                	        }
                	      }
                	}
                	// attribute
                    
                    for(int k=0; k < caseLineList.getLength(); k++) {
                    	
                        Element firstNameElement = (Element)caseLineList.item(k);

                        NodeList text = firstNameElement.getChildNodes();
                        
                        System.out.println("Line" + k + ": " + ((Node)text.item(0)).getNodeValue().trim());
                        
                        // Attribute
                    	if(firstNameElement.hasAttributes()) {
                    		NamedNodeMap attrs = firstNameElement.getAttributes();
                    		for(int u = 0 ; u<attrs.getLength() ; u++) {
                    	        Attr attribute = (Attr)attrs.item(u);     
                    	        System.out.println("Attribut: " + attribute.getName() + " = "+attribute.getValue());
                    	      }
                    	}
                    	// attribute
                    }              
                }            	
            }
			
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			giveSignBack(player, sign);
		}
	}

	private void giveSignBack(Player player, Block sign) {
		sign.setType(Material.AIR);
		ItemStack currentItem = player.getItemInHand();
		currentItem.setType(Material.SIGN);
		player.setItemInHand(currentItem);

	}

	private String getColor(String colorName, Player player) {

		if (colorName.equals("black"))
			return "§0";
		// BLACK
		else if (colorName.equals("navy"))
			return "§1";
		// DARK_BLUE
		else if (colorName.equals("green"))
			return "§2";
		// DARK_GREEN
		else if (colorName.equals("blue"))
			return "§3";
		// DARK_AQUA
		else if (colorName.equals("red"))
			return "§4";
		// DARK_RED
		else if (colorName.equals("purple"))
			return "§5";
		// DARK_PURPLE
		else if (colorName.equals("gold"))
			return "§6";
		// GOLD
		else if (colorName.equals("lightgray"))
			return "§7";
		// GRAY
		else if (colorName.equals("gray"))
			return "§8";
		// DARK_GRAY
		else if (colorName.equals("darkpurple"))
			return "§9";
		// BLUE
		else if (colorName.equals("lightgreen"))
			return "§a";
		// GREEN
		else if (colorName.equals("lightblue"))
			return "§b";
		// AQUA
		else if (colorName.equals("rose"))
			return "§c";
		// RED
		else if (colorName.equals("lightpurple"))
			return "§d";
		// LIGHT_PURPLE
		else if (colorName.equals("yellow"))
			return "§e";
		// YELLOW
		else if (colorName.equals("white"))
			return "§f";
		// WHITE
		else {
			// if a wrong color is passed we set the default to white
			player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-color"));
		}
		return "§f";
	}

	public boolean isIntNumber(String num) {
		try {
			Integer.parseInt(num);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}
