package databasePart1;

import application.User;
import application.Answer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 * CHANGES MADE:
 * added new table userRoles to store multiple roles per user
 * updated 'register(User user)' to insert roles into both 'cse360users' and 'userRoles'
 * updated 'login(User user) to load all roles from 'userRoles' instead of just one
 *	(if no entry exists in userRoles, fallback to single role in cse360users)
 * added helper method 'getUserRoles(String userName)' to retrieve multiple roles
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase"; 

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
    
	    // Create the one-time passwords table
	    String otpTable = "CREATE TABLE IF NOT EXISTS OneTimePasswords ("
		    + "userName VARCHAR(255), "
		    + "otp VARCHAR(255), "
		    + "expiration TIMESTAMP, "
		    + "isUsed BOOLEAN DEFAULT FALSE, "
		    + "PRIMARY KEY(userName, otp))";
	    statement.execute(otpTable);
	    }
	}
	// Admin sets a one-time password for a user who forgot theirs
	public boolean setOneTimePassword(String userName, String otp, Timestamp expiration) {
		String query = "INSERT INTO OneTimePasswords (userName, otp, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Validate a one-time password for a user (not expired, not used)
	public boolean validateOneTimePassword(String userName, String otp) {
		String query = "SELECT expiration FROM OneTimePasswords WHERE userName = ? AND otp = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					markOneTimePasswordAsUsed(userName, otp);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Mark a one-time password as used
	private void markOneTimePasswordAsUsed(String userName, String otp) {
		String query = "UPDATE OneTimePasswords SET isUsed = TRUE WHERE userName = ? AND otp = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, otp);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "name VARCHAR(255), "
	            + "email VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);
	  //new table for multiple roles
	    String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255), "
				+ "role VARCHAR(20),"
				+ "FOREIGN KEY (userName) REFERENCES cse360users(userName))";
		statement.execute(userRolesTable);

		// Create the invitation codes table
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
			+ "code VARCHAR(10) PRIMARY KEY, "
			+ "email VARCHAR(255), "
			+ "expiration TIMESTAMP, "
			+ "isUsed BOOLEAN DEFAULT FALSE)";
		statement.execute(invitationCodesTable);

		// Create the one-time passwords table
		String otpTable = "CREATE TABLE IF NOT EXISTS OneTimePasswords ("
			+ "userName VARCHAR(255), "
			+ "otp VARCHAR(255), "
			+ "expiration TIMESTAMP, "
			+ "isUsed BOOLEAN DEFAULT FALSE, "
			+ "PRIMARY KEY(userName, otp))";
		statement.execute(otpTable);
		
		// Create Questions table
	    String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
	        + "id INT AUTO_INCREMENT PRIMARY KEY, "
	        + "askedBy VARCHAR(255), "
	        + "text VARCHAR(1000), "
	        + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	        + "isResolved BOOLEAN DEFAULT FALSE, "
	        + "followUpOf INT, "
	        + "FOREIGN KEY (askedBy) REFERENCES cse360users(userName))";
	    statement.execute(questionsTable);

	    // Create Answers table
	    String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
	        + "id INT AUTO_INCREMENT PRIMARY KEY, "
	        + "questionId INT, "
	        + "answeredBy VARCHAR(255), "
	        + "text VARCHAR(2000), "
	        + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	        + "isRead BOOLEAN DEFAULT FALSE, "
	        + "isChosen BOOLEAN DEFAULT FALSE, "
	        + "FOREIGN KEY (questionId) REFERENCES Questions(id), "
	        + "FOREIGN KEY (answeredBy) REFERENCES cse360users(userName))";
	    statement.execute(answersTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password,name,email, role) VALUES (?,?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getUserInfoName());  
	        pstmt.setString(4, user.getEmail());  
	        pstmt.setString(5, user.getRole());
	        pstmt.executeUpdate();
		}
	
	//insert into userRoles for full role list
			String insertRole = "INSERT INTO UserRoles (userName, role) VALUES (?,?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertRole)) {
		        for (String role : user.getRoles()) {
		            pstmt.setString(1, user.getUserName());
		            pstmt.setString(2, role);
		            pstmt.executeUpdate();
		        }
			}
		}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					//login successful

					//step 2: clear and load roles from UserRoles
					user.getRoles().clear();
					List<String>roles = getUserRoles(user.getUserName());
					if(!roles.isEmpty()) {
						for (String role: roles) {
							user.addRole(role);
						} 
					}else {
						//fallback: is UserRoles is empty, use cse360users.role
							String singleRole = rs.getString("role");
							if(singleRole != null ) {
								user.addRole(singleRole);
							}

					}
					return true;
				}
			}
		}
		//if login fails check for OTP
		query = "SELECT * FROM CSE360USERS AS cu INNER JOIN ONETIMEPASSWORDS AS ot ON cu.USERNAME = ot.USERNAME WHERE cu.USERNAME = ? AND ot.OTP = ? AND cu.ROLE = ? AND NOT ot.ISUSED AND ot.EXPIRATION > CURRENT_TIMESTAMP()";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					//login successful

					//step 2: clear and load roles from UserRoles
					user.getRoles().clear();
					List<String>roles = getUserRoles(user.getUserName());
					if(!roles.isEmpty()) {
						for (String role: roles) {
							user.addRole(role);
						} 
					}else {
						//fallback: is UserRoles is empty, use cse360users.role
							String singleRole = rs.getString("role");
							if(singleRole != null ) {
								user.addRole(singleRole);
							}

					}
					//set OTP as used
					markOneTimePasswordAsUsed(user.getUserName(), user.getPassword());
					return true;
				}
			}
		}
		return false;
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	//Retrieves multiple roles from user from database using their Username.
		public List<String> getUserRoles(String userName){
			List<String> roles = new ArrayList<>();
			String query = "SELECT role FROM UserRoles WHERE userName = ?";
			try(PreparedStatement pstmt = connection.prepareStatement(query)){
				pstmt.setString(1,userName);
				ResultSet rs = pstmt.executeQuery();

				while(rs.next()) {
					String role = rs.getString("role");
					roles.add(role);


				}
				return roles;

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return roles;
		}
	
	// Generates a new invitation code and inserts it into the database.

	// Generates a new invitation code, associates it with an email and expiration, and inserts it into the database.
	public String generateInvitationCode(String email, Timestamp expiration) {
		if (!isValidEmail(email)) {
			throw new IllegalArgumentException("Invalid email format");
		}
		String code = UUID.randomUUID().toString().substring(0, 6); // 6-char code
		String query = "INSERT INTO InvitationCodes (code, email, expiration) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			pstmt.setString(2, email);
			pstmt.setTimestamp(3, expiration);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	// Validates email format (simple regex)
	public static boolean isValidEmail(String email) {
		return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	// Validates expiration date string (yyyy-MM-dd HH:mm)
	public static boolean isValidExpiration(String expiration) {
		return expiration != null && expiration.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");
	}
	
	// Validates an invitation code to check if it is unused.
	// Validates an invitation code to check if it is unused and not expired.
	public boolean validateInvitationCode(String code) {
		String query = "SELECT expiration FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp expiration = rs.getTimestamp("expiration");
				if (expiration != null && expiration.after(new Timestamp(System.currentTimeMillis()))) {
					// Mark the code as used
					markInvitationCodeAsUsed(code);
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// Method to get all users
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT * FROM cse360users";
		try (PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				User user = new User(
						rs.getString("userName"),
						rs.getString("password"),
						rs.getString("name"),
						rs.getString("email"),
						rs.getString("role") // we can add other fields as needed here
				);
				users.add(user);
			}
		}
		return users;
	}
	
	// Method to delete a user
	public boolean deleteUser(String userName, String currentAdmin) throws SQLException {
	    // Prevent admin from deleting themselves
	    if (userName.equals(currentAdmin)) {
	        return false;
	    }
	    connection.setAutoCommit(false); 
	    try {
	        String deleteRolesQuery = "DELETE FROM UserRoles WHERE userName = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(deleteRolesQuery)) {
	            pstmt.setString(1, userName);
	            pstmt.executeUpdate();
	        }

	        String deleteUserQuery = "DELETE FROM cse360users WHERE userName = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(deleteUserQuery)) {
	            pstmt.setString(1, userName);
	            int rowsAffected = pstmt.executeUpdate();
	            
	            connection.commit(); 
	            return rowsAffected > 0;
	        }

	    } catch (SQLException e) {
	        connection.rollback(); 
	        throw e; 
	    } finally {
	        connection.setAutoCommit(true);
	    }
	} //UPDATED SINCE CODE WAS NOT WORKING JA/OD
	
	// Method to update user roles
	public boolean updateUserRole(String userName, String newRole, String currentAdmin) throws SQLException {
		// Prevent an admin from removing their own admin role
		if (userName.equals(currentAdmin) && !newRole.contains("admin")) {
			// Check if there's at least one other admin
			if (countAdmins() <= 1) {
				return false;
			}
		}
		
		String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newRole);
			pstmt.setString(2,  userName);;
			return pstmt.executeUpdate() > 0;
		}
	}
	
	private int countAdmins() throws SQLException {
		String query = "SELECT COUNT(*) FROM cse360users WHERE role = 'admin'";
		try (PreparedStatement pstmt = connection.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
	// Insert new question
	public int addQuestion(String askedBy, String text) throws SQLException {
	    String query = "INSERT INTO Questions (askedBy, text) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setString(1, askedBy);
	        pstmt.setString(2, text);
	        pstmt.executeUpdate();
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1); // return questionId
	        }
	    }
	    return -1;
	}
	

	// Get all questions as List<String>
	public List<String> getAllQuestionsFromDB() throws SQLException {
	    List<String> questions = new ArrayList<>();
	    String query = "SELECT id, text, isResolved, followUpOf FROM Questions ORDER BY id ASC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String text = rs.getString("text");
	            boolean resolved = rs.getBoolean("isResolved");
	            Integer followUpOf = rs.getObject("followUpOf") != null ? rs.getInt("followUpOf") : null;
	            String s = "Q[" + id + "]" + (resolved ? " [RESOLVED]" : "")
	                    + (followUpOf != null ? " (Follow-up of Q[" + followUpOf + "])" : "")
	                    + ": " + text;
	            questions.add(s);
	        }
	    }
	    return questions;
	}
	
	// Get all questions ordered newest first
	public List<String> getAllQuestionsFromDBOrderedByNewest() throws SQLException {
	    List<String> questions = new ArrayList<>();
	    String query = "SELECT id, text, isResolved, followUpOf FROM Questions ORDER BY createdAt DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String text = rs.getString("text");
	            boolean resolved = rs.getBoolean("isResolved");
	            Integer followUpOf = rs.getObject("followUpOf") != null ? rs.getInt("followUpOf") : null;
	            String s = "Q[" + id + "]" + (resolved ? " [RESOLVED]" : "")
	                    + (followUpOf != null ? " (Follow-up of Q[" + followUpOf + "])" : "")
	                    + ": " + text;
	            questions.add(s);
	        }
	    }
	    return questions;
	}

	// Add follow-up question
	public int addFollowUpQuestion(int parentId, String askedBy, String text) throws SQLException {
	    String query = "INSERT INTO Questions (askedBy, text, followUpOf) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setString(1, askedBy);
	        pstmt.setString(2, text);
	        pstmt.setInt(3, parentId);
	        pstmt.executeUpdate();
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) return rs.getInt(1);
	    }
	    return -1;
	}

	// Update getUnresolvedQuestions to include follow-ups and ordering
	public List<String> getUnresolvedQuestions() throws SQLException {
	    List<String> questions = new ArrayList<>();
	    String query = "SELECT id, text, followUpOf FROM Questions WHERE isResolved = FALSE ORDER BY createdAt DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String text = rs.getString("text");
	            Integer followUpOf = rs.getObject("followUpOf") != null ? rs.getInt("followUpOf") : null;
	            String s = "Q[" + id + "]" + (followUpOf != null ? " (Follow-up of Q[" + followUpOf + "])" : "")
	                    + ": " + text;
	            questions.add(s);
	        }
	    }
	    return questions;
	}

	// Mark question as resolved
	public boolean markQuestionResolved(int questionId) throws SQLException {
	    String query = "UPDATE Questions SET isResolved = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        return pstmt.executeUpdate() > 0;
	    }
	}
	
	// Insert new answer
	public int addAnswer(int questionId, String answeredBy, String text) throws SQLException {
	    String query = "INSERT INTO Answers (questionId, answeredBy, text) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setInt(1, questionId);
	        pstmt.setString(2, answeredBy);
	        pstmt.setString(3, text);
	        pstmt.executeUpdate();
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1); // return answerId
	        }
	    }
	    return -1;
	}

	// Get answers for a question
	public List<Answer> getAnswersForQuestion(int questionId) throws SQLException {
	    List<Answer> answers = new ArrayList<>();
	    String query = "SELECT id, text, isRead, isChosen, answeredBy FROM Answers WHERE questionId = ? ORDER BY createdAt ASC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, questionId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Answer answer = new Answer(
	                rs.getInt("id"),
	                questionId,
	                rs.getString("answeredBy"),
	                rs.getString("text"),
	                rs.getBoolean("isRead"),
	                rs.getBoolean("isCHosen")
	            );
	            answers.add(answer);
	        }
	    }
	    return answers;
	}

	// Mark answer as read
	public boolean markAnswerRead(int answerId) throws SQLException {
	    String query = "UPDATE Answers SET isRead = TRUE WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, answerId);
	        return pstmt.executeUpdate() > 0;
	    }
	}

	// Accept an answer
	public boolean acceptAnswer(int questionId, int answerId) throws SQLException {
	    connection.setAutoCommit(false);
	    try {
	        // First, mark all answers for this question as not accepted
	        String resetQuery = "UPDATE Answers SET isChosen = FALSE WHERE questionId = ?";
	        try (PreparedStatement resetStmt = connection.prepareStatement(resetQuery)) {
	            resetStmt.setInt(1, questionId);
	            resetStmt.executeUpdate();
	        }

	        // Now, set the chosen answer as accepted
	        String acceptQuery = "UPDATE Answers SET isChosen = TRUE WHERE id = ? AND questionId = ?";
	        try (PreparedStatement acceptStmt = connection.prepareStatement(acceptQuery)) {
	            acceptStmt.setInt(1, answerId);
	            acceptStmt.setInt(2, questionId);
	            int updated = acceptStmt.executeUpdate();

	            if (updated > 0) {
	                // Mark the question as resolved
	                markQuestionResolved(questionId);
	                connection.commit();
	                return true;
	            }
	        }
	    } catch (SQLException e) {
	        connection.rollback();
	        throw e;
	    } finally {
	        connection.setAutoCommit(true);
	    }
	    return false;
	}
	
	public boolean updateUserRoles(String userName, List<String> newRoles, String currentAdmin) throws SQLException {
	    // Prevent an admin from removing their own admin role if they're the last admin
	    if (userName.equals(currentAdmin) && !newRoles.contains("admin")) {
	        if (countAdmins() <= 1) {
	            return false;
	        }
	    }
	    
	    // Use transaction to ensure consistency
	    connection.setAutoCommit(false);
	    try {
	        // Delete all existing roles for this user
	        String deleteQuery = "DELETE FROM UserRoles WHERE userName = ?";
	        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
	            deleteStmt.setString(1, userName);
	            deleteStmt.executeUpdate();
	        }
	        
	        // Insert the new roles
	        String insertQuery = "INSERT INTO UserRoles (userName, role) VALUES (?, ?)";
	        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
	            for (String role : newRoles) {
	                insertStmt.setString(1, userName);
	                insertStmt.setString(2, role);
	                insertStmt.executeUpdate();
	            }
	        }
	        
	        // Update the primary role in cse360users (use the first role)
	        String updateQuery = "UPDATE cse360users SET role = ? WHERE userName = ?";
	        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
	            updateStmt.setString(1, newRoles.isEmpty() ? null : newRoles.get(0));
	            updateStmt.setString(2, userName);
	            updateStmt.executeUpdate();
	        }
	        
	        connection.commit();
	        return true;
	    } catch (SQLException e) {
	        connection.rollback();
	        throw e;
	    } finally {
	        connection.setAutoCommit(true);
	    }
	}

}
