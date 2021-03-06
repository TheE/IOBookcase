package de.minehattan.uripura.IOBookcase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.entity.Player;

public class IOBookcaseDatabase {

	private Connection connection;
	private Statement statement;
	private ResultSet rs;

	public IOBookcaseDatabase() {

		try {
			Class.forName( "org.sqlite.JDBC");
			connection = DriverManager.getConnection( "jdbc:sqlite:plugins/IOBookcase/bookcase.db");

		} catch( SQLException e) {
			System.out.println( "Coud not find Database");
			e.printStackTrace();
		} catch( ClassNotFoundException e) {
			System.out.println( "Coud not find Database driver");
			e.printStackTrace();
		}
	}

	public void writeSql( String textToWrite, int lineNum, String worldName, int x, int y, int z) {

		boolean checkUpdate;
		String lineName = "line" + lineNum;

		try {
			statement = connection.createStatement();
			rs = statement.executeQuery( "SELECT `world` FROM `bookshelf` WHERE `world`='" + worldName + "' AND `locx` =" + x + " AND `locy` = " + y + " AND `locz` = " + z + ";");

			// Check for any results
			if( rs.isBeforeFirst() != false)
				checkUpdate = true;
			else
				checkUpdate = false;

			textToWrite = textToWrite.replace( "'", "''");
			textToWrite = textToWrite.replace( ";", ":");

			if( checkUpdate == true)
				statement.executeUpdate( "UPDATE bookshelf SET `" + lineName + "`='" + textToWrite + "' WHERE `world`='" + worldName + "' AND `locx`=" + x + " AND `locy`=" + y + " AND `locz`=" + z + ";");
			else
				statement.executeUpdate( "INSERT INTO bookshelf (`world`,`" + lineName + "`,`locx`,`locy`,`locz`) VALUES ('" + worldName + "', '" + textToWrite + "', " + x + ", " + y + ", " + z + ");");

		} catch( SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void readCase( Player player, String worldName, int x, int y, int z) {

		String sendback = null;

		try {
			statement = connection.createStatement();

			rs = statement.executeQuery( "SELECT `line1`, `line2`, `line3`, `line4`, `line5`, `line6`, `line7`, `line8`, `line9`, `line10` FROM `bookshelf` WHERE `world`='" + worldName + "' AND `locx` = " + x + " AND `locy` = " + y + " AND `locz` = " + z + ";");

			for( int i = 0; i < 10; i++) {
				sendback = rs.getString( "line" + (i + 1));
				if( sendback != null)
					player.sendMessage( sendback);
				sendback = null;
			}

		} catch( SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkCase( String worldName, int x, int y, int z) {

		boolean check = false;

		try {
			statement = connection.createStatement();
			rs = statement.executeQuery( "SELECT `world` FROM `bookshelf` WHERE `world`='" + worldName + "' AND `locx`=" + x + " AND `locy` = " + y + " AND `locz` = " + z + ";");

			if( rs.isBeforeFirst() != false)
				check = true;

		} catch( SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
		return check;
	}

	public void deleteCase( String worldName, int x, int y, int z) {

		try {
			statement = connection.createStatement();

			statement.executeUpdate( "DELETE FROM bookshelf WHERE `world`='" + worldName + "' AND `locx`=" + x + " AND `locy`=" + y + " AND `locz`=" + z);

		} catch( SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateCase( String worldName, int oldX, int oldY, int oldZ, int x, int y, int z) {

		try {
			statement = connection.createStatement();

			statement.executeUpdate( "UPDATE bookshelf SET `locx`=" + x + ", `locy`=" + y + ",`locz`=" + z + ", ' WHERE `world`='" + worldName + "' AND `locx`=" + x + " AND `locy`=" + y + " AND `locz`=" + z + ";");

		} catch( SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean createDatabase() {
		try {
			statement = connection.createStatement();

			statement.execute( "CREATE TABLE IF NOT EXISTS bookshelf (`world` varchar(32)," + "`locx` INT, `locy` INT, `locz` INT, "
					+ "`line1` varchar(32), `line2` varchar(32), `line3` varchar(32), `line4` varchar(32), `line5` varchar(32),"
					+ "`line6` varchar(32), `line7` varchar(32), `line8` varchar(32), `line9` varchar(32), `line10` varchar(32));");

			return true;
		} catch( SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				statement.close();
			} catch( SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch( SQLException e) {

			e.printStackTrace();
		}
	}
}
