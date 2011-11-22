package de.minehattan.uripura.IOBookcase;

/*
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class IOBookcaseBlockListener extends BlockListener {

	private IOBookcase plugin;

	public IOBookcaseBlockListener(IOBookcase instance) {
		this.plugin = instance;
	}

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
		if (player.hasPermission("iobookcasereborn.write") == false)
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
			player.sendRawMessage(firstLine);
			player.sendMessage(bufferText.toString());
			handleImport(player, block, sign, firstLine, bufferText.toString());

		} else
			player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-format"));
	}

	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		boolean checkcase = false;
		String worldName = event.getPlayer().getWorld().getName();

		if (block.getType() == Material.BOOKSHELF && !event.isCancelled()) {
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
					// Drop a Bookcase for the player
					block.getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.BOOKSHELF, 1));
				}
			} catch (Exception e) {
				System.out.println("delete fail: " + e);
			}

		}
	}

	private void handleLine(Player player, Block block, Block sign, String firstLine, String otherLines) {
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
		 */
		if (firstLineString.length > 1) {
			// If it's bigger than 1 we probably got a line number
			// if(firstLineString[1])
			// Character.isNumber(firstLineString[1]);
			if(isIntNumber(firstLineString[1])) {
				lineNum = Integer.parseInt(firstLineString[1]);
			}
			else {
				player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-format"));
				return;
			}
			
			if (firstLineString.length > 2) {
				// If it's bigger than 2 we got an additional color
				firstLineString[2] = firstLineString[2].toLowerCase();
				lineColor = getColor(firstLineString[2], player);
			}
		} else {
			player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-to-few-arguments"));
			return;
		}

		// Create the String that is send o the Database
		textToWrite = lineColor + otherLines;

		if (lineNum < 11 && lineNum > 0) {
			/*
			 * The text is safe to be send to the database
			 */
			IOBookcaseDatabase connection = new IOBookcaseDatabase();
			connection.writeSql(textToWrite, lineNum, worldName, block.getX(), block.getY(), block.getZ());
			player.sendMessage(ChatColor.YELLOW + plugin.getConfig().getString("msg-notify-written") + " " + lineNum);

			// Delete the sign and give it back to the player
			giveSignBack(player, sign);

		} else
			player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-error-lines"));

	}

	private void handleImport(Player player, Block bookcase, Block sign, String firstLine, String otherLines) {
		// import from text file
		if (firstLine.contains("@import")) {
			// String sentCaseName = event.getLine(1);
			String sentCaseName = otherLines;
			File importFile = new File(plugin.getDataFolder() + "/import.txt");
			String line;
			int indexoflinenum = 0;
			int indexnum = 0;
			String importlinecolor = "";
			String fileCaseName = "";
			boolean foundCase = false;
			boolean insideline = false;
			boolean insidetag = false;

			//
			int x = bookcase.getX();
			int y = bookcase.getY();
			int z = bookcase.getZ();
			boolean checkcase = false;
			String worldName = player.getWorld().getName();
			IOBookcaseDatabase connection = new IOBookcaseDatabase();
			String lineColor = "§f";
			//
			String[] casetext = { null, null, null, null, null, null, null, null, null, null };

			try {
				BufferedReader br = new BufferedReader(new FileReader(importFile));

				while ((line = br.readLine()) != null) {
					// opening <case> tag
					if (line.contains("<case") && !insidetag) {
						// player.sendMessage("Looking for <case>");
						indexoflinenum = line.indexOf("name=\"");
						indexoflinenum = indexoflinenum + 6;

						if (indexoflinenum > -1) {
							fileCaseName = line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum));
							// player.sendMessage("casename: "+fileCaseName);
							// the case they entered on the sign matches the one we
							// have in the file
							if (sentCaseName.matches(fileCaseName)) {
								foundCase = true;
								player.sendMessage("Found " + fileCaseName);
							}
							// not this cases' name, move on
						} else {
							// if the case does not have a name attribute in the import
							// file
							player.sendMessage("Could not find the case's name, make sure your format is correct");
						}

						insidetag = true;
					} // end if <case

					// set the insidetag flag to false if we've reached the closing
					// case tag
					else if (insidetag && line.contains("</case>")) {
						// player.sendMessage("Looking for </case>");
						insidetag = false;
						foundCase = false;
						fileCaseName = "";
					}

					// read the <line> tags since we found the case
					else if (insidetag && foundCase && !insideline) {
						// player.sendMessage("Looking for <line>");
						if (line.contains("<line")) {
							// finds the line num - mandatory
							indexoflinenum = line.indexOf("num=\"");
							indexoflinenum = indexoflinenum + 5;
							if (indexoflinenum > -1) {
								indexnum = Integer.parseInt(line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum)));
								indexnum = indexnum - 1; // convert line number to java
																	// array index
								// player.sendMessage("Reading line "+(indexnum+1));
								insideline = true;
							} else
								// if the case does not have a name attribute in the
								// import file
								player.sendMessage("Could not find the line's number, make sure your format is correct");

							// finds the line color - optional
							indexoflinenum = line.indexOf("color=\"");
							indexoflinenum = indexoflinenum + 7;
							if (indexoflinenum > -1) {
								// private String getColor(String colorName, Player
								// player)

								importlinecolor = line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum));

								lineColor = getColor(importlinecolor, player);

							}
						}
					}

					else if (insideline && !line.contains("</line>") && foundCase && !line.contains("</case>")) {
						// player.sendMessage("Looking for <line> contents: "+fileCaseName);
						if (indexnum > -1 && indexnum < 10) {
							casetext[indexnum] = lineColor + line.trim();
							// player.sendMessage("Line "+(indexnum+1)+": "+line.trim());
						} else {
							player.sendMessage("The line number " + indexnum + " is not between 1 and 10.");
						}
					}

					else if (insideline && line.contains("</line>") && foundCase) {
						// player.sendMessage("Looking for </line>");
						insideline = false;
					}

					// we are inside a case tag that was not matched, do nothing.
					else {
						// player.sendMessage("No action required");
					}

				} // end file while loop
				int i = 0;

				if (casetext[0] == null) {
					player.sendMessage("The case with name " + sentCaseName + " was not found.");
				} else {

					checkcase = connection.checkCase(worldName, x, y, z);

					if (checkcase) {
						connection.deleteCase(worldName, x, y, z);
						player.sendMessage(ChatColor.RED + plugin.getConfig().getString("msg-notify-deleted"));

					}

					// i is for the string (from 0 to 9)
					// and j is for the linenum in the database (from 1 to 10)
					int j;
					for (j = 1, i = 0; i < 10; i++, j++) {
						if (casetext[i] != null) {
							//player.sendMessage(casetext[i] + ", " + j + ", " + worldName + ", " + bookcase.getY());
							connection.writeSql(casetext[i], j, worldName, bookcase.getX(), bookcase.getY(), bookcase.getZ());
							
						}
					}

					player.sendMessage("lines written to the case " + bookcase.getX() + "," + bookcase.getY() + ","
							+ bookcase.getZ());
				}
				
				// somehow this works...
				// connection.writeSql("test", 5, "minehattan", 11, 21, 31);

				// Delete the sign and give it back to the player
				giveSignBack(player, sign);

			} catch (IOException e) {
				/*
				 * player.sendMessage(ChatColor.RED +
				 * "An error occured reading the import file.");
				 * player.sendMessage(ChatColor.RED +
				 * "Please check your server console for more details.");
				 */System.out.println("Error reading import file: " + e);
			}

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
