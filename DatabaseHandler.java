import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;

class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:users.db";
    
    static {
        createNewDatabase();
        createUserTable();
    }
    
    private static void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Database created successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
        }
    }
    
    private static void createUserTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table created successfully");
            
            // Insert default test user if not exists
            String checkUser = "SELECT username FROM users WHERE username = 'user'";
            ResultSet rs = stmt.executeQuery(checkUser);
            if (!rs.next()) {
                String insertDefault = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertDefault)) {
                    pstmt.setString(1, "user");
                    pstmt.setString(2, hashPassword("$"));
                    pstmt.executeUpdate();
                    System.out.println("Default user created");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String inputHash = hashPassword(password);
                return storedHash.equals(inputHash);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }
}