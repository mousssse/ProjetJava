package main.java.com.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import main.java.com.controller.listener.ChatListener;
import main.java.com.controller.listener.DBListener;
import main.java.com.controller.listener.LoginListener;
import main.java.com.model.Message;
import main.java.com.model.User;

/**
 * The DBManager manages the local user's database.
 * @author sarah
 * @author Sandro
 *
 */
public class DBManager implements DBListener, LoginListener, ChatListener {
	private static DBManager dbManager = null;
	private String url = "jdbc:sqlite:chatApp.db";
	private Connection conn;

	private DBManager() {
		super();
		this.connect();
		this.initTables();
	}
	
	public static DBManager getInstance() {
		if (dbManager == null) dbManager = new DBManager();
		return dbManager;
	}
	
	/**
	 * Connects to the local database
	 */
	private void connect() {
		try {
			Class.forName("org.sqlite.JDBC");
			this.conn = DriverManager.getConnection(this.url);			
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the necessary tables if not already in the database
	 */
	private void initTables () {
        String sql = "CREATE TABLE IF NOT EXISTS messages (\n"
                + " content TEXT NOT NULL, \n"
                + " time TEXT NOT NULL, \n"
                + " fromId TEXT NOT NULL, \n"
                + " toId TEXT NOT NULL, \n"
                + " PRIMARY KEY (fromId, toId, time) \n"
                + ");";
        try { 
        	Statement stmt = conn.createStatement();
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        sql = "CREATE TABLE IF NOT EXISTS users (\n"
                + "id TEXT PRIMARY KEY, \n"
                + "username TEXT NOT NULL UNIQUE, \n"
                + "password TEXT \n"
                + ");";
        try {
            Statement stmt = conn.createStatement();
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Inserts a given user in the local database
     * 
     * @param id The id of the user we are adding
     * @param username The username of the user we are adding
     */
    public void insertUser(String username, String id) {
        String sql = "INSERT OR IGNORE INTO users(id, username, password) VALUES(?, ?, ?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, username);
            pstmt.setString(3, null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
        	if (e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
        		// TODO: user is already in db!
        	}
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * The database will only be updated when the user first logs in
     * 
     * @param username The username of the local user
     * @param hashedPassword A hash of the local user's password
     */
    public void insertThisUser(String username, String hashedPassword) {
    	String sqlCheckExisting = "SELECT * FROM users WHERE password IS NOT NULL;";
    	
    	try {
    		Statement stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(sqlCheckExisting);
            if (rs.isBeforeFirst()) {
    			// local user already exists in the database, no need to insert them
            	return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    	// This is the first time the user logs in
        String sql = "INSERT OR IGNORE INTO users(id, username, password) VALUES(?, ?, ?)";
        String id = UUID.randomUUID().toString();

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * @param id The id of the user we want to update
     * @param newUsername The new username of the user
     */
    public void updateUsername(String id, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?;";

        try {
        	PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newUsername);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
        	if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
        		// TODO: username is already taken!
        		// check that only when changing your own username?
        	}
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Inserts a message in the messages table
     * 
     * @param content A <code>Blob</code> object that contains the content of the message
     * @param time A string that represents the time of the message
     * @param fromId The user id of the sender
     * @param toId The user id of the receiver
     */
    public void insertMessage(String content, String time, String fromId, String toId) {
    	String sql = "INSERT INTO messages(content, time, fromId, toId) VALUES(?, ?, ?, ?);";
    	
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, time);
            pstmt.setString(3, fromId);
            pstmt.setString(4, toId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Gets the messages of a conversation with a remote user
     * 
     * @param remoteUserId The user id with whom we want to check our messages
     * @return A ResultSet of the messages database rows that correspond
     * @throws SQLException
     */
    public ResultSet getConversationHistory(String remoteUserId) throws SQLException {
    	String sql = "SELECT * FROM messages WHERE toId = ? OR fromId = ? ORDER BY time ASC;";
    	
		PreparedStatement pstmt = conn.prepareStatement(sql);
	    pstmt.setString(1, remoteUserId);
	    pstmt.setString(2, remoteUserId);
	    return pstmt.executeQuery();
    }
    
    /**
     * Finds the username corresponding to the given id
     * 
     * @param id The id of the user
     * @return The username associated to the user id
     */
    public String getUsernameFromId(String id) {
    	String sql = "SELECT username FROM users WHERE id = ?;";
    	
    	try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("username");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	return "";
    }
    
    /**
     * Finds the id corresponding to the given username
     * 
     * @param username The username of the user
     * @return The id associated to the username
     */
    public String getIdFromUsername(String username) {
    	String sql = "SELECT id FROM users WHERE username = ?;";
    	
    	try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString("id");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	return "";
    }
    
    /**
     * Returns the hashed password of the local user
     * 
     * @param id The id of the local user
     * @return The hashed password stored in the local database
     * @throws SQLException
     */
    public String getHashedPassword(String id) throws SQLException {
    	String sql = "SELECT password FROM users WHERE id = ?;";
    	PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        ResultSet rs = pstmt.executeQuery();
        return rs.getString("password");
    }

	@Override
	public void onSelfLogin(String username, String password) {
		this.insertThisUser(username, password);
		ListenerManager.getInstance().fireOnSelfLoginNext(username);
	}

	@Override
	public void onLogin(User remoteUser) {
		this.insertUser(remoteUser.getUsername(), remoteUser.getId());
		
	}

	@Override
	public void onLogout(User remoteUser) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onChatRequest(User remoteUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChatClosure(User remoteUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageToSend(User localUser, User remoteUser, String messageContent, LocalDateTime date) {
		this.insertMessage(messageContent, date.toString(), localUser.getId(), remoteUser.getId());
	}

	@Override
	public void onMessageToReceive(Message message) {
		this.insertMessage(message.getContent(), message.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), message.getFromUser().getId(), message.getToUser().getId());
	}
	
    // Used for tests
	public static void main(String[] args) {
		DBManager manager = DBManager.getInstance();
		System.out.println(manager.getIdFromUsername("sarah"));
		manager.insertThisUser("sarah", "password");
		manager.insertUser("sandro", "thisisanid");
		System.out.println(manager.getUsernameFromId("thisisanid"));
		System.out.println(manager.getIdFromUsername("sandro"));
		//String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		//manager.insertMessage("hello sandro", now, manager.getIdFromUsername("sarah"), manager.getIdFromUsername("sandro"));
		//manager.insertMessage("how are you?", now, manager.getIdFromUsername("sarah"), manager.getIdFromUsername("sandro"));
		//manager.insertMessage("hi! fine, and you?", now, manager.getIdFromUsername("sandro"), manager.getIdFromUsername("sarah"));
		try {
			ResultSet history = manager.getConversationHistory("thisisanid");
			while(history.next()) {
				String fromUsername = manager.getUsernameFromId(history.getString("fromId"));
				System.out.println("from " + fromUsername + ": " + history.getString("content"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
