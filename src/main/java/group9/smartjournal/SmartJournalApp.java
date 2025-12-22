package group9.smartjournal;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class SmartJournalApp {
    // We store all loaded users here
    private static ArrayList<User> users = new ArrayList<>();
    private static User currentUser = null; // Keep track of who is logged in

    // Store environment variables
    public static java.util.Map<String, String> env;
    private static API api = new API();

    public static void main (String[] args) {
        // 1. Load Env
        env = EnvLoader.loadEnv("src/main/.env");

        // 2. Load data from text file
        loadUserData();

        // 3. Start the login process
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Journaling System");

        while (currentUser == null) {
            System.out.print("Please enter your email to login: ");
            String emailInput = inputScanner.nextLine();

            System.out.print("Please enter your password: ");
            String inputPassword = inputScanner.nextLine();

            if (login(emailInput, inputPassword)) {
                System.out.println("Login Successful!");
                showWelcomePage();
            } else
                System.out.println("Invalid email or password. Please try again.\n");
        }
    }

    // Method to read the text file
    public static void loadUserData () {
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

    // Method to check credentials
    public static boolean login (String email, String password) {
        // Loop through the loaded users list
        for (User user : users) {
            // Login by email
            if (user.getEmail().equals(email) &&  user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }

        return false;
    }

    // Shows welcome page
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
            String weather = extractWeatherForecast(response);

            System.out.println("Current Weather: " + weather);

        } catch (Exception e) {
            System.out.println("Could not fetch weather.");
        }

        System.out.println("==========================================");
        showMainMenu();
    }

    private static void showMainMenu () {
        // Placeholder for the menu options required later
        System.out.println("1. Create, Edit & View Journals");
        System.out.println("2. View Weekly Mood Summary");
        System.out.println("3. Exit"); // Good practice to have an exit
        System.out.print("Choose an option: ");
    }

    // --- HELPER 1: Extract Weather ---
    // Target: "summary_forecast":"Cloudy" -> We want "Cloudy"
    private static String extractWeatherForecast (String jsonResponse) {
        String key = "\"summary_forecast\":\"";
        int startIndex = jsonResponse.indexOf(key);

        if (startIndex == -1) return "Weather data unavailable"; // Safety check

        // Move start index to the beginning of the actual value
        startIndex += key.length();

        // Find the closing quote "
        int endIndex = jsonResponse.indexOf("\"", startIndex);

        return jsonResponse.substring(startIndex, endIndex);
    }

    // --- HELPER 2: Extract Mood ---
    // Tip from Assignment : "The label that has the highest score will display first"
    // So we just need to find the FIRST "label" pattern we see.
    private static String extractMoodLabel(String jsonResponse) {
        String key = "\"label\":\"";
        int startIndex = jsonResponse.indexOf(key);

        if (startIndex == -1) return "Mood unavailable"; // Safety check

        startIndex += key.length();
        int endIndex = jsonResponse.indexOf("\"", startIndex);

        return jsonResponse.substring(startIndex, endIndex);
    }
}
