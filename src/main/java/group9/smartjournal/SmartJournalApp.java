package group9.smartjournal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SmartJournalApp {
    // We store all loaded users here
    private static ArrayList<User> users = new ArrayList<>();
    private static User currentUser = null; // Keep track of who is logged in

    public static void main (String[] args) {
        // 1. Load data from text file
        loadUserData();

        // 2. Start the login process
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Journaling System");

        while (currentUser == null) {
            System.out.print("Please enter your email to login: ");
            String emailInput = inputScanner.nextLine();

            System.out.print("Please enter your password: ");
            String inputPassword = inputScanner.nextLine();

            if (login(emailInput, inputPassword)) {
                System.out.println("Login Successful!");
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
}
