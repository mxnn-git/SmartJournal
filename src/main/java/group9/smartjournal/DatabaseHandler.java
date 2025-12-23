package group9.smartjournal;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler {
    // Connection string. 'journal.db' is automatically created.
    private static final String URL = "jdbc:sqlite:journal.db";

    // 1. Connect to database
    private Connection connect () {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(URL);

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }

        return conn;
    }

    // 2. Initialise: Create the table if it doesn't exist
    public void createTable () {
        // SQL command to create a table with columns: date, email, content, mood, weather
        String sql = "CREATE TABLE IF NOT EXISTS journals (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "date TEXT NOT NULL, " +
                     "email TEXT NOT NULL, " +
                     "content TEXT, " +
                     "mood TEXT, " +
                     "weather TEXT)";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("Database initialised successfully.");

        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    // 3. Save a Journal Entry
    public void saveJournal (JournalEntry entry) {
        String sql = "INSERT INTO journals(date, email, content, mood, weather) VALUES(?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getDate());
            pstmt.setString(2, entry.getEmail());
            pstmt.setString(3, entry.getContent());
            pstmt.setString(4, entry.getMood());
            pstmt.setString(5, entry.getWeather());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving journal: " + e.getMessage());
        }
    }

    // 4. Load Journals
    public ArrayList<JournalEntry> loadJournals () {
        ArrayList<JournalEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM journals";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new JournalEntry(
                        rs.getString("date"),
                        rs.getString("email"),
                        rs.getString("content"),
                        rs.getString("mood"),
                        rs.getString("weather")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error loading journals: " + e.getMessage());
        }

        return list;
    }

    // 5. Update Journal
    public void updateJournal (JournalEntry entry) {
        String sql = "UPDATE journal SET content = ?, mood = ? WHERE email = ? AND date = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getContent());
            pstmt.setString(2, entry.getMood());
            pstmt.setString(3, entry.getEmail());
            pstmt.setString(4, entry.getDate());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error updating journal: " + e.getMessage());
        }
    }

}
