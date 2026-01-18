package group9.smartjournal;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

public class JournalLogic {
    // Shared Data
    public static User currentUser = null;
    public static ArrayList<JournalEntry> journals = new ArrayList<>();
    private static ArrayList<User> users = new ArrayList<>();

    // Tools
    private static Map<String, String> env;
    private static API api = new API();
    private static DatabaseHandler db = new DatabaseHandler();

    // --- INITIALIZATION ---
    public static void init() {
        env = EnvLoader.loadEnv(".env");
        db.createTable();
        journals = db.loadJournals();
        loadLocalUsers(); // Helper to load users from text file
    }

    // --- AUTHENTICATION ---
    public static boolean login(String email, String rawPass) {
        String hashed = hashPassword(rawPass);
        for (User u : users) {
            if (u.getEmail().equals(email) && u.getPassword().equals(hashed)) {
                currentUser = u;
                return true;
            }
        }
        return false;
    }

    public static String register(String email, String name, String rawPass) {
        // FIX: Changed && to || so it fails if EITHER symbol is missing
        if (!email.contains("@") || !email.contains(".")) {
            return "Invalid email format (must contain '@' and '.').";
        }

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) return "Email already exists.";
        }

        String hashed = hashPassword(rawPass);
        User newUser = new User(email, name, hashed);
        users.add(newUser);

        // Save to file
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("UserData.txt", true))) {
            pw.println();
            pw.println(email);
            pw.println(name);
            pw.println(hashed);
            pw.println();
        } catch (Exception e) { return "File Error"; }

        return "Success";
    }

    // --- API FEATURES ---
    public static String getMood(String content) {
        try {
            String url = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";
            String jsonBody = "{\"inputs\": \"" + content.replace("\"", "'") + "\"}";
            String response = api.post(url, env.get("BEARER_TOKEN"), jsonBody);

            // Extract Label
            String key = "\"label\":\"";
            int start = response.indexOf(key) + key.length();
            return response.substring(start, response.indexOf("\"", start));
        } catch (Exception e) { return "Unknown"; }
    }

    public static String getWeather() {
        try {
            String url = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String response = api.get(url);

            // Extract
            String key = "\"summary_forecast\":\"";
            int start = response.indexOf(key) + key.length();
            String raw = response.substring(start, response.indexOf("\"", start));

            // Simplify
            String lower = raw.toLowerCase();
            if (lower.contains("ribut") || lower.contains("storm")) return "Stormy";
            if (lower.contains("hujan") || lower.contains("rain")) return "Rainy";
            if (lower.contains("mendung") || lower.contains("cloud")) return "Cloudy";
            if (lower.contains("cerah") || lower.contains("no rain")) return "Sunny";
            return raw.length() > 10 ? raw.substring(0, 10) + "..." : raw;
        } catch (Exception e) { return "Unavailable"; }
    }

    // --- DATA MANAGEMENT ---
    public static void saveEntry(String date, String content, String mood, String weather) {
        JournalEntry entry = new JournalEntry(date, currentUser.getEmail(), content, mood, weather);
        journals.add(entry);
        db.saveJournal(entry);
    }

    public static void updateEntry(JournalEntry entry) {
        db.updateJournal(entry);
    }

    public static JournalEntry getEntryForDate(String date) {
        for (JournalEntry e : journals) {
            if (e.getEmail().equals(currentUser.getEmail()) && e.getDate().equals(date)) {
                return e;
            }
        }
        return null;
    }

    // --- HELPERS ---
    private static void loadLocalUsers() {
        java.io.File file = new java.io.File("UserData.txt");
        try (java.util.Scanner s = new java.util.Scanner(file)) {
            while (s.hasNextLine()) {
                String e = s.nextLine().trim();
                if (e.isEmpty() || !s.hasNextLine()) continue;
                String n = s.nextLine();
                if (!s.hasNextLine()) break;
                String p = s.nextLine();
                users.add(new User(e, n, p));
            }
        } catch (Exception e) {}
    }

    private static String hashPassword(String pass) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pass.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // --- SEARCH FEATURE ---
    public static java.util.List<String> searchJournals(String keyword) {
        java.util.List<String> matches = new ArrayList<>();
        String lowerKey = keyword.toLowerCase();

        for (JournalEntry entry : journals) {
            // Check if entry belongs to user AND contains the keyword
            if (entry.getEmail().equals(currentUser.getEmail()) &&
                    entry.getContent().toLowerCase().contains(lowerKey)) {
                matches.add(entry.getDate());
            }
        }
        // Sort matches (optional, but good)
        java.util.Collections.sort(matches, java.util.Collections.reverseOrder());
        return matches;
    }

    // --- DELETE FEATURE ---
    public static void deleteEntry(String date) {
        // 1. Find the entry object in memory
        JournalEntry toRemove = null;
        for (JournalEntry e : journals) {
            if (e.getEmail().equals(currentUser.getEmail()) && e.getDate().equals(date)) {
                toRemove = e;
                break;
            }
        }

        // 2. Remove it
        if (toRemove != null) {
            journals.remove(toRemove); // Remove from list
            db.deleteJournal(currentUser.getEmail(), date); // Remove from DB
        }
    }
}
