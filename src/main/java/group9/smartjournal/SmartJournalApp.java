package group9.smartjournal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class SmartJournalApp {

    // Store all loaded users here
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<JournalEntry> journals = new ArrayList<>();
    private static User currentUser = null; // Keep track of who is logged in

    // Store environment variables
    public static java.util.Map<String, String> env;
    private static API api = new API();

    public static void main (String[] args) {

         /*
         // TEMP: Generate hashes for the assignment users
         System.out.println("Hash for pw-Stud#1: " + hashPassword("pw-Stud#1"));
         System.out.println("Hash for pw-Stud#2: " + hashPassword("pw-Stud#2"));
         System.exit(0); // Uncomment this if you just want to see the hashes and stop

         s100201@student.fop
         Foo Bar
         pw-Stud#1

         s100202@student.fop
         John Doe
         pw-Stud#2
         */

        // 1. Load Env
        env = EnvLoader.loadEnv("src/main/.env");

        // 2. Load data from text file
        loadUserData();
        loadJournalData();

        // 3. Start the login process
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Journaling System");

        while (currentUser == null) {
            System.out.println("\n1. Login");
            System.out.println("2. Register New Account");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            String choice = inputScanner.nextLine();

            switch (choice) {
                case "1" -> {
                    System.out.print("Please enter your email to login: ");
                    String emailInput = inputScanner.nextLine();

                    System.out.print("Please enter your password: ");
                    String passwordInput = inputScanner.nextLine();

                    if (login(emailInput, passwordInput)) {
                        System.out.println("Login Successful!");
                        showWelcomePage();
                    } else {
                        System.out.println("Invalid email or password. Please try again.");
                    }
                }
                case "2" -> registerUser();
                case "3" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // === Login - Registration / Main Menu ===

    public static void loadUserData () {
        // Method to read the text file
        // RELATIVE PATH: We just use the path name. Java looks in the project root
        File file = new File("src/main/UserData.txt");

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String email = fileScanner.nextLine().trim(); // Read and remove extra spaces

                // DATA HANDLING: If we hit an empty line (the gap), skip this iteration
                if (email.isEmpty())
                    continue;

                // If we have an email, assume the next two lines MUST be Name and Password
                if (!fileScanner.hasNextLine()) break;
                String name = fileScanner.nextLine();

                if(!fileScanner.hasNextLine()) break;
                String password = fileScanner.nextLine();

                // Create user object and add to the list
                users.add(new User(email, name, password));
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error: UserData.txt not found.");
            System.out.println("Please ensure UserData.txt is in the project root folder.");
        }
    }

    private static void registerUser () {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== User Registration ===");

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        // Check for duplicate emails
        for (User u : users) {
            if(u.getEmail().equalsIgnoreCase(email)) {
                System.out.println("Error: Email already registered!");
                return;
            }
        }

        System.out.print("Enter Display Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Password: ");
        String rawPassword = scanner.nextLine();

        // Hash the password
        String hashedPassword = hashPassword(rawPassword);

        // Add to in-memory list
        users.add(new User(email, name, hashedPassword));

        // Append the text file
        try (java.io.FileWriter fw = new FileWriter("src/main/UserData.txt", true);
             java.io.PrintWriter pw = new PrintWriter(fw)) {

            pw.println(email);
            pw.println(name);
            pw.println(hashedPassword);
            pw.println();

            System.out.println("Registration Successful! You can now login.");

        } catch (java.io.IOException e) { System.out.println("Error saving new user."); }

    }

    public static boolean login (String email, String rawPassword) {
        // Method to check credentials
        // 1. Hash the input password immediately
        String hashedPassword = hashPassword(rawPassword);

        for (User user : users) {
            // 2. Compare the hashed input vs the stored hash in the file
            if (user.getEmail().equals(email) && user.getPassword().equals(hashedPassword)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    private static String hashPassword (String originalPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(originalPassword.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void showWelcomePage () {
        // Get the current time
        java.time.LocalTime now = java.time.LocalTime.now();
        String greeting;

        // Determine greeting based on hour
        if (now.isBefore(LocalTime.NOON)) {
            // 00:00 to 11:59 (Before 12:00 PM)
            greeting = "Good Morning";
        } else if (now.isBefore(java.time.LocalTime.of(17, 0))) {
            // 12:00 to 16:59 (Before 5:00 PM)
            greeting = "Good Afternoon";
        } else {
            // 17:00 onwards
            greeting = "Good Evening";
        }

        System.out.println("\n" + greeting + ", " + currentUser.getDisplayName() + "!");

        // Fetch and show weather
        try {
            System.out.println("Fetching weather for Kuala Lumpur...");

            // URL provided in assignment for KL
            String url = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String response = api.get(url);
            String rawWeather = extractWeatherForecast(response);
            String weather = simplifyWeather(rawWeather);

            System.out.println("Current Weather: " + weather);

        } catch (Exception e) {
            System.out.println("Could not fetch weather.");
        }

        System.out.println("==========================================");
        showMainMenu();
    }

    private static void showMainMenu () {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Create, Edit & View Journals");
            System.out.println("2. View Weekly Mood Summary");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> handleJournalFeature();
                case "2" -> showWeeklySummary();
                case "3" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // === Core Journal Features ===

    private static void handleJournalFeature () {
        Scanner scanner = new Scanner(System.in);
        java.time.LocalDate today = java.time.LocalDate.now();

        while (true) {
            System.out.println("\n=== Journal Dates ===");
            // 1. Show past 3 days + Today
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                String label = (i == 0) ? " (Today)" : "";
                System.out.println((7 - i) + ". " + date + label);
            }
            System.out.println("8. Enter Specific Date");
            System.out.println("9. Back to Main Menu");
            System.out.print("Select a date to view/create: ");

            String choice = scanner.nextLine();
            if (choice.equals("9")) return; // Go back

            // Logic for specific date
            if (choice.equals("8")) {
                System.out.print("Enter date (YYYY-MM-DD): ");
                String manualDate = scanner.nextLine();
                processDateSelection(manualDate);
                continue;
            }

            try {
                int daysToSubtract = 7 - Integer.parseInt(choice);

                if (daysToSubtract < 0 | daysToSubtract > 6) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                String selectedDate = today.minusDays(daysToSubtract).toString();
                processDateSelection(selectedDate);

            } catch (NumberFormatException e) {
                System.out.println("Invalid Input.");
            }
        }
    }

    private static void processDateSelection (String date) {
        // 1. Check if we already have an entry for this user & date
        JournalEntry existingEntry = null;
        for (JournalEntry entry : journals) {
            if (entry.getEmail().equals(currentUser.getEmail()) && entry.getDate().equals(date)) {
                existingEntry = entry;
                break;
            }
        }

        Scanner scanner = new Scanner(System.in);

        if (existingEntry == null) {
            // CASE A: No entry exists.
            // You can only create entries for TODAY (usually).
            // The assignment implies creating "for today" ,
            // but let's allow creating for past dates too for flexibility if you want.
            System.out.println("\nNo entry found for " + date);
            System.out.println("1. Create New Entry");
            System.out.println("2. Back");
            System.out.print("> ");

            if (scanner.nextLine().equals("1")) {
                createNewJournal(date);
            }
        } else {
            // CASE B: Entry exists.
            System.out.println("\nEntry exists for " + date);
            System.out.println("Mood: " + existingEntry.getMood()); // Show the AI mood!
            System.out.println("1. View Journal");
            System.out.println("2. Edit Journal");
            System.out.println("3. Back");
            System.out.print("> ");

            String subChoice = scanner.nextLine();
            if (subChoice.equals("1")) {
                System.out.println("\n--- Journal Content ---");
                System.out.println(existingEntry.getContent());
                System.out.println("-----------------------");
                System.out.println("Press Enter to go back...");
                scanner.nextLine();
            } else if (subChoice.equals("2")) {
                editJournal(existingEntry);
            }
        }
    }

    private static void createNewJournal (String date) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nEnter your journal entry for " + date + ":");
        System.out.print("> ");
        String content = scanner.nextLine();

        // 1. ANALYZE MOOD
        System.out.println("Analyzing mood and weather...");
        String mood = "Unknown";
        try {
            String url = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";
            String jsonBody = "{\"inputs\": \"" + content.replace("\"", "'") + "\"}";
            String token = env.get("BEARER_TOKEN");
            String response = api.post(url, token, jsonBody);
            mood = extractMoodLabel(response);

        } catch (Exception e) {
            System.out.println("AI Analysis failed.");
        }

        // 2. FETCH WEATHER (NEW!)
        String weather = "Unknown";
        try {
            String url = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String response = api.get(url);
            String rawWeather = extractWeatherForecast(response);
            weather = simplifyWeather(rawWeather);

        } catch (Exception e) {
            System.out.println("Weather fetch failed.");
        }

        // 3. SAVE EVERYTHING
        JournalEntry newEntry = new JournalEntry(date, currentUser.getEmail(), content, mood, weather);
        journals.add(newEntry);
        saveJournalData();
        System.out.println("Journal saved! Mood: " + mood + " | Weather: " + weather);
    }

    private static void editJournal (JournalEntry entry) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nOriginal: " + entry.getContent());
        System.out.println("Enter new content: ");
        System.out.print("> ");
        String newContent = scanner.nextLine();

        // AI Again
        System.out.println("Re-analysing mood...");
        String newMood = "Unknown";

        try {
            String url = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";
            String jsonBody = "{\"inputs\": \"" + newContent.replace("\"", "'") + "\"}";
            String token = env.get("BEARER_TOKEN");
            String response = api.post(url, token, jsonBody);
            newMood = extractMoodLabel(response);

        } catch (Exception e) {
            System.out.println("AI Analysis failed. Keeping old mood.");
            newMood = entry.getMood();
        }

        entry.setContent(newContent);
        entry.setMood(newMood);
        saveJournalData();
        System.out.println("Journal updated! New Mood: " + newMood);
    }

    private static void loadJournalData () {
        // DATA STORAGE: LOAD
        File file = new File("src/main/JournalData.txt");
        if(!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Split by "|" symbol. We use \\| because | is a special regex character
                String[] parts = line.split("\\|", 5);

                if (parts.length == 5) {
                    String date = parts[0];
                    String email = parts[1];
                    String mood = parts[2];
                    String weather = parts[3];
                    String content = parts[4];

                    journals.add(new JournalEntry(date, email, content, mood, weather));
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading journal data.");
        }
    }

    private static void saveJournalData() {
        // --- DATA STORAGE: SAVE ---
        // We rewrite the whole file every time (Simple but effective for small data)
        try (java.io.PrintWriter writer = new java.io.PrintWriter("src/main/JournalData.txt")){
            for (JournalEntry entry : journals) {
                writer.println(entry.toFileString());
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error saving journal data.");
        }
    }

    // === API / AI Features ===

    private static String extractWeatherForecast (String jsonResponse) {
        // --- HELPER 1: Extract Weather ---
        // Target: "summary_forecast":"Cloudy" -> We want "Cloudy"
        String key = "\"summary_forecast\":\"";
        int startIndex = jsonResponse.indexOf(key);

        if (startIndex == -1) return "Weather data unavailable"; // Safety check

        // Move start index to the beginning of the actual value
        startIndex += key.length();

        // Find the closing quote "
        int endIndex = jsonResponse.indexOf("\"", startIndex);

        return jsonResponse.substring(startIndex, endIndex);
    }

    public static String simplifyWeather (String rawWeather) {
        // Helper to convert raw API text into simple English
        if (rawWeather == null) return "Unknown";

        // Convert to lowercase
        String lower = rawWeather.toLowerCase();

        if (lower.contains("berjerebu")) {
            return "Hazy";
        } else if (lower.contains("hujan")) {
            return "Rainy";
        } else if (lower.contains("ribut")) {
            return "Stormy";
        } else if (lower.contains("tiada hujan")) {
            return "Sunny";
        } else {
            // If we don't recognize it, just return the first 10 chars of the original so it fits the table
            return rawWeather.length() > 10 ? rawWeather.substring(0, 10) + "..." : rawWeather;
        }
    }

    private static String extractMoodLabel(String jsonResponse) {
        // --- HELPER 2: Extract Mood ---
        // Tip from Assignment : "The label that has the highest score will display first" So we just need to find the FIRST "label" pattern we see.
        String key = "\"label\":\"";
        int startIndex = jsonResponse.indexOf(key);

        if (startIndex == -1) return "Mood unavailable"; // Safety check

        startIndex += key.length();
        int endIndex = jsonResponse.indexOf("\"", startIndex);

        return jsonResponse.substring(startIndex, endIndex);
    }

    private static void showWeeklySummary () {
        System.out.println("\n=== Weekly Mood & Weather Summary ===");
        System.out.printf("%-15s %-15s %-30s%n", "Date", "Mood", "Weather"); // Table Header
        System.out.println("-------------------------------------------------------------");

        java.time.LocalDate today = java.time.LocalDate.now();

        // Loop for the past 7 days (including today)
        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).toString();

            // Find entry for this day
            JournalEntry foundEntry = null;
            for (JournalEntry entry : journals) {
                if (entry.getEmail().equals(currentUser.getEmail()) && entry.getDate().equals(dateStr)) {
                    foundEntry = entry;
                    break;
                }
            }

            if (foundEntry != null) {
                System.out.printf("%-15s %-15s %-30s%n", dateStr, foundEntry.getMood(), foundEntry.getWeather());
            } else {
                System.out.printf("%-15s %-15s %-30s%n", dateStr, "-", "-");
            }
        }

        System.out.println("-------------------------------------------------------------");

        System.out.println("Press Enter to return to menu...");
        new Scanner(System.in).nextLine();
    }

}
