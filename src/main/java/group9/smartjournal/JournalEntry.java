package group9.smartjournal;

public class JournalEntry {
    private String date;    // Format: YYYY-MM-DD
    private String email;   // To link the entry to a specific user
    private String content;
    private String mood;    // Extracted from AI
    private String weather;

    public JournalEntry (String date, String email, String content, String mood, String weather) {
        this.date = date;
        this.email = email;
        this.content = content;
        this.mood = mood;
        this.weather = weather;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public String getEmail() { return email; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getWeather() { return weather; }

    // Format: Date|Email|Mood|Content
    public String toFileString() {
        return date + "|" + email + "|" + mood + "|" + weather + "|" + content;
    }
}
