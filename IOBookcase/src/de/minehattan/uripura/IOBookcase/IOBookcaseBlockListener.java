package de.minehattan.uripura.IOBookcase;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
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

	public IOBookcaseBlockListener( IOBookcase instance) {
		this.plugin = instance;
	}

	@EventHandler( priority = EventPriority.HIGH)
	public void onSignChange( SignChangeEvent event) {
		// Stores the first line of the sign
		String firstLine = null;
		// Stores the left lines of the sign
		StringBuffer bufferText = new StringBuffer();

		Player player = event.getPlayer();
		Block block = event.getBlock();
		Block sign = event.getBlock();
		boolean signOnCase = false;

		if( event.isCancelled())
			return;
		if( player.hasPermission( "iobookcase.write") == false)
			return;

		// Check if the Sign was placed on one of the Sides...
		if( block.getRelative( BlockFace.NORTH).getType() == Material.BOOKSHELF) {
			block = block.getRelative( BlockFace.NORTH);
			signOnCase = true;
		} else if( block.getRelative( BlockFace.EAST).getType() == Material.BOOKSHELF) {
			block = block.getRelative( BlockFace.EAST);
			signOnCase = true;
		} else if( block.getRelative( BlockFace.SOUTH).getType() == Material.BOOKSHELF) {
			block = event.getBlock().getRelative( BlockFace.SOUTH);
			signOnCase = true;
		} else if( block.getRelative( BlockFace.WEST).getType() == Material.BOOKSHELF) {
			block = block.getRelative( BlockFace.WEST);
			signOnCase = true;
		}
		// ...if not: stop here
		if( signOnCase == false)
			return;

		// Store the first line in firstLine and the rest in bufferText
		firstLine = event.getLine( 0).toLowerCase();
		for( int i = 1; i < 4; i++) {
			bufferText.append( event.getLine( i));
		}

		// Check if the first line contains @line or @import
		if( firstLine.contains( "@line")) {
			handleLine( player, block, firstLine, bufferText.toString());
			// Delete the sign and give it back to the player
			giveSignBack( player, sign);
		} else if( firstLine.contains( "@import")) {
			handleImport( player, block, firstLine, bufferText.toString());
			// Delete the sign and give it back to the player
			giveSignBack( player, sign);
		} else
			player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-format"));
	}

	@EventHandler( priority = EventPriority.HIGH)
	public void onBlockBreak( BlockBreakEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = event.getPlayer().getWorld().getName();

		if( block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			checkcase = connection.checkCase( worldName, x, y, z);

			if( checkcase) {
				connection.deleteCase( worldName, x, y, z);
				event.getPlayer().sendMessage( ChatColor.YELLOW + plugin.getConfig().getString( "msg-notify-deleted"));

			}
			if( plugin.getConfig().getBoolean( "drop-bookcase")) {
				// We cancel the event and drop a normal bookcase
				event.setCancelled( true);
				block.setType( Material.AIR);
				if( event.getPlayer().getGameMode() != GameMode.CREATIVE) {
					block.getWorld().dropItemNaturally( event.getBlock().getLocation(), new ItemStack( Material.BOOKSHELF, 1));
				}
			}
		} catch( Exception e) {
			plugin.errorMessage( "Unable to delete a bookcase");
		}

	}

	@EventHandler( priority = EventPriority.MONITOR)
	public void onBlockBurn( BlockBurnEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = block.getWorld().getName();

		if( block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			checkcase = connection.checkCase( worldName, x, y, z);

			if( checkcase) {
				connection.deleteCase( worldName, x, y, z);
				plugin.warnMessage( "A Bookcase died in a fire in " + worldName + " at x:" + x + " y:" + y + " z:" + z);
			}

		} catch( Exception e) {
			plugin.errorMessage( "Unable to delete a bookcase");
		} finally {
			connection.closeConnection();
		}
	}

	@EventHandler( priority = EventPriority.MONITOR)
	public void onBlockPistonExtend( BlockPistonExtendEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = block.getWorld().getName();

		if( block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;

		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			checkcase = connection.checkCase( worldName, x, y, z);

			if( checkcase) {
				plugin.warnMessage( "A Bookcase was moved in " + worldName + " at x:" + x + " y:" + y + " z:" + z);
			}

		} catch( Exception e) {
			plugin.errorMessage( "Unable to update a bookcase on a PistonExtendEvent");
		} finally {
			connection.closeConnection();
		}
	}
	
	@EventHandler( priority = EventPriority.MONITOR)
	public void onBlockPistonRetract( BlockPistonRetractEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = block.getWorld().getName();
		
		if( block.getType() != Material.BOOKSHELF || event.isCancelled())
			return;
		
		IOBookcaseDatabase connection = new IOBookcaseDatabase();

		try {
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();

			checkcase = connection.checkCase( worldName, x, y, z);

			if( checkcase) {
				plugin.warnMessage( "A Bookcase was moved in " + worldName + " at x:" + x + " y:" + y + " z:" + z);
			}

		} catch( Exception e) {
			plugin.errorMessage( "Unable to update a bookcase on a PistonRetractEvent");
		} finally {
			connection.closeConnection();
		}
	}
	
	private void handleLine( Player player, Block block, String firstLine, String otherLines) {
		// Line number in the bookcase
		int lineNum = 1;
		// Color of the line (default = white)
		String lineColor = "§f";
		// String that is send to the database
		String textToWrite;
		// For splitting the first line
		String[] firstLineString;
		// Name of the current world
		String worldName = player.getWorld().getName();

		firstLineString = firstLine.split( " ");

		/*
		 * Format: @line NUMBER [COLOR_AS_STRING] If the array contains more
		 * than 1 string we probably got a line number
		 */
		if( firstLineString.length > 1) {

			// Check whether this string is numeric or not
			if( isIntNumber( firstLineString[1])) {
				lineNum = Integer.parseInt( firstLineString[1]);
			} else {
				player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-format"));
				return;
			}

			// If it's bigger than 2 we got an additional color
			if( firstLineString.length > 2) {
				lineColor = getColor( firstLineString[2], player);
			}
		} else {
			player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-to-few-arguments"));
			return;
		}

		// Create the String that is send to the Database
		textToWrite = lineColor + otherLines;

		if( lineNum < 11 && lineNum > 0) {
			/*
			 * The text is safe to be send to the database
			 */
			IOBookcaseDatabase connection = new IOBookcaseDatabase();
			connection.writeSql( textToWrite, lineNum, worldName, block.getX(), block.getY(), block.getZ());
			player.sendMessage( ChatColor.YELLOW + plugin.getConfig().getString( "msg-notify-written") + " " + lineNum);

			// Finally close the Connection
			connection.closeConnection();

		} else
			player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-lines"));

	}

	private void handleImport( Player player, Block bookcase, String firstLine, String otherLines) {
		// String that is send to the database
		String textToWrite = "";
		// Name of the current world
		String worldName = player.getWorld().getName();
		// Set to true if the import was successful
		Boolean found = false;
		// Our Database-Object
		IOBookcaseDatabase connection = new IOBookcaseDatabase();
		// Number of the Line we currently got
		int lineNum = 1;
		// Stores the color of the text
		String txtColor = "";
		// Stored the name of the case
		String caseName = "";

		// import from text file
		if( firstLine.contains( "@import")) {
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse( plugin.getDataFolder() + File.separator + "import.xml");

				// normalize text representation
				doc.getDocumentElement().normalize();

				NodeList cases = doc.getElementsByTagName( "case");

				int numberOfCases = cases.getLength();

				for( int caseNum = 0; caseNum < numberOfCases; caseNum++) {
					Node caseNode = cases.item( caseNum);

					if( caseNode.getNodeType() == Node.ELEMENT_NODE) {
						Element caseElement = ( Element) caseNode;
						NodeList caseLineList = caseElement.getElementsByTagName( "line");

						// Attribute in which the name of the case is stored
						if( caseElement.hasAttributes()) {
							NamedNodeMap attrs = caseElement.getAttributes();
							// Search for the name attribute and set the caseName
							for( int i = 0; i < attrs.getLength(); i++) {
								Attr attribute = ( Attr) attrs.item( i);
								if( attribute.getName().equals( "name")) {
									caseName = attribute.getValue();
								}
							}
						}

						for( int caseLineNum = 0; caseLineNum < caseLineList.getLength(); caseLineNum++) {
							Element caseLine = ( Element) caseLineList.item( caseLineNum);
							NodeList text = caseLine.getChildNodes();

							// Get the text thats in the line
							String txtLine = ( ( Node) text.item( 0)).getNodeValue().trim();

							// Get all attributes that are in the line element
							if( caseLine.hasAttributes()) {
								NamedNodeMap attrs = caseLine.getAttributes();

								if( otherLines.equals( caseName)) {
									for( int u = 0; u < attrs.getLength(); u++) {
										Attr attribute = ( Attr) attrs.item( u);

										// Search for the num and color
										if( attribute.getName().equals( "num")) {
											// Check if it is really a number
											// if its not we improvise on the
											// lineNumber
											if( isIntNumber( attribute.getValue()))
												lineNum = Integer.parseInt( attribute.getValue());
											else
												lineNum = caseLineNum;
										} else if( attribute.getName().equals( "color"))
											txtColor = attribute.getValue();
									}
									if( lineNum < 11 && lineNum > 0) {
										// Build the text string for the import
										textToWrite = getColor( txtColor, player) + txtLine;
										// The text is safe to be send to the
										// database

										connection.writeSql( textToWrite, lineNum, worldName, bookcase.getX(), bookcase.getY(), bookcase.getZ());
										found = true;
									} else
										player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-lines"));
								}
							}
						}
					}
				}
				if( found == false)
					player.sendMessage( ChatColor.YELLOW + plugin.getConfig().getString( "msg-error-import"));
				else
					player.sendMessage( ChatColor.YELLOW + plugin.getConfig().getString( "msg-notify-found"));

			} catch( ParserConfigurationException e) {
				e.printStackTrace();
			} catch( SAXException e) {
				e.printStackTrace();
			} catch( IOException e) {
				e.printStackTrace();
			} finally {
				connection.closeConnection();
			}
		}
	}

	private void giveSignBack( Player player, Block sign) {
		sign.setType( Material.AIR);
		ItemStack currentItem = player.getItemInHand();
		currentItem.setType( Material.SIGN);
		player.setItemInHand( currentItem);

	}

	private String getColor( String colorName, Player player) {
		if( colorName.equalsIgnoreCase( "black"))
			return "§0";
		// BLACK
		else if( colorName.equalsIgnoreCase( "navy"))
			return "§1";
		// DARK_BLUE
		else if( colorName.equalsIgnoreCase( "green"))
			return "§2";
		// DARK_GREEN
		else if( colorName.equalsIgnoreCase( "blue"))
			return "§3";
		// DARK_AQUA
		else if( colorName.equalsIgnoreCase( "red"))
			return "§4";
		// DARK_RED
		else if( colorName.equalsIgnoreCase( "purple"))
			return "§5";
		// DARK_PURPLE
		else if( colorName.equalsIgnoreCase( "gold"))
			return "§6";
		// GOLD
		else if( colorName.equalsIgnoreCase( "lightgray"))
			return "§7";
		// GRAY
		else if( colorName.equalsIgnoreCase( "gray"))
			return "§8";
		// DARK_GRAY
		else if( colorName.equalsIgnoreCase( "darkpurple"))
			return "§9";
		// BLUE
		else if( colorName.equalsIgnoreCase( "lightgreen"))
			return "§a";
		// GREEN
		else if( colorName.equalsIgnoreCase( "lightblue"))
			return "§b";
		// AQUA
		else if( colorName.equalsIgnoreCase( "rose"))
			return "§c";
		// RED
		else if( colorName.equalsIgnoreCase( "lightpurple"))
			return "§d";
		// LIGHT_PURPLE
		else if( colorName.equalsIgnoreCase( "yellow"))
			return "§e";
		// YELLOW
		else if( colorName.equalsIgnoreCase( "white"))
			return "§f";
		// WHITE
		else {
			// if a wrong color is passed we set the default to white
			player.sendMessage( ChatColor.RED + plugin.getConfig().getString( "msg-error-color"));
		}
		return "§f";
	}

	public boolean isIntNumber( String num) {
		try {
			Integer.parseInt( num);
		} catch( NumberFormatException e) {
			return false;
		}
		return true;
	}
}
