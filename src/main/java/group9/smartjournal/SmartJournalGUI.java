package group9.smartjournal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class SmartJournalGUI extends JFrame {

    // --- STATE ---
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);

    // --- COMPONENTS ---
    private JTextArea journalContentArea;
    private JTextField emailField, regEmailField, regNameField;
    private JPasswordField passField, regPassField;
    private JTextField searchField;

    // Split Lists
    private JList<String> weekList;
    private DefaultListModel<String> weekListModel;
    private JList<String> historyList;
    private DefaultListModel<String> historyListModel;

    private JLabel welcomeLabel, weatherLabel, moodLabel;
    private JButton saveBtn, deleteBtn; // <-- Added Delete Button

    // --- COLORS ---
    private final Color BG_MAIN = new Color(245, 247, 250);
    private final Color BG_CARD = Color.WHITE;
    private final Color TEXT_PRIMARY = Color.BLACK;
    private final Color BORDER_COLOR = new Color(200, 200, 200);

    // --- FONTS ---
    private final String FONT_NAME = "SansSerif";
    private final Font FONT_TITLE = new Font(FONT_NAME, Font.BOLD, 24);
    private final Font FONT_BOLD  = new Font(FONT_NAME, Font.BOLD, 13);
    private final Font FONT_PLAIN = new Font(FONT_NAME, Font.PLAIN, 13);
    private final Font FONT_BTN   = new Font(FONT_NAME, Font.BOLD, 11);
    private final Font FONT_LINK  = new Font(FONT_NAME, Font.PLAIN, 11);

    public SmartJournalGUI() {
        setTitle("Smart Journal System");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JournalLogic.init();

        mainContainer.add(createLoginScreen(), "LOGIN");
        mainContainer.add(createRegisterScreen(), "REGISTER");
        mainContainer.add(createDashboardScreen(), "DASHBOARD");

        add(mainContainer);
        setVisible(true);
    }

    // --- 1. LOGIN SCREEN ---
    private JPanel createLoginScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_MAIN);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(50, 80, 50, 80)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Smart Journal", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        emailField = createTextField();
        passField = createPasswordField();

        JButton loginBtn = createButton("LOG IN", new Color(240, 240, 240), TEXT_PRIMARY);
        JButton regBtn = createFlatButton("Create new account");
        JButton exitBtn = createFlatButton("Exit");
        exitBtn.setForeground(Color.RED);

        card.add(title, gbc);
        gbc.gridy++; card.add(createLabel("Email Address"), gbc);
        gbc.gridy++; card.add(emailField, gbc);
        gbc.gridy++; card.add(createLabel("Password"), gbc);
        gbc.gridy++; card.add(passField, gbc);
        gbc.gridy++; gbc.insets = new Insets(25, 0, 10, 0);
        card.add(loginBtn, gbc);
        gbc.gridy++; gbc.insets = new Insets(5, 0, 0, 0);
        card.add(regBtn, gbc);
        gbc.gridy++; card.add(exitBtn, gbc);

        container.add(card);

        loginBtn.addActionListener(e -> {
            if (JournalLogic.login(emailField.getText(), new String(passField.getPassword()))) {
                passField.setText("");
                refreshDashboard();
                cardLayout.show(mainContainer, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email or Password");
            }
        });
        regBtn.addActionListener(e -> cardLayout.show(mainContainer, "REGISTER"));
        exitBtn.addActionListener(e -> System.exit(0));

        return container;
    }

    // --- 2. REGISTER SCREEN ---
    private JPanel createRegisterScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_MAIN);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        regEmailField = createTextField();
        regNameField = createTextField();
        regPassField = createPasswordField();
        JButton signUpBtn = createButton("SIGN UP", new Color(240, 240, 240), TEXT_PRIMARY);
        JButton backBtn = createFlatButton("Back to Login");

        card.add(title, gbc);
        gbc.gridy++; card.add(createLabel("Email"), gbc);
        gbc.gridy++; card.add(regEmailField, gbc);
        gbc.gridy++; card.add(createLabel("Display Name"), gbc);
        gbc.gridy++; card.add(regNameField, gbc);
        gbc.gridy++; card.add(createLabel("Password"), gbc);
        gbc.gridy++; card.add(regPassField, gbc);

        gbc.gridy++; gbc.insets = new Insets(25, 0, 10, 0);
        card.add(signUpBtn, gbc);
        gbc.gridy++; gbc.insets = new Insets(5, 0, 0, 0);
        card.add(backBtn, gbc);

        container.add(card);

        signUpBtn.addActionListener(e -> {
            String res = JournalLogic.register(regEmailField.getText(), regNameField.getText(), new String(regPassField.getPassword()));
            if (res.equals("Success")) {
                JOptionPane.showMessageDialog(this, "Account Created! Please Login.");
                cardLayout.show(mainContainer, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, res);
            }
        });
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        return container;
    }

    // --- 3. DASHBOARD ---
    private JPanel createDashboardScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_MAIN);

        // -- TOP BAR --
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_CARD);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        topBar.setPreferredSize(new Dimension(0, 50));

        welcomeLabel = new JLabel(" Dashboard");
        welcomeLabel.setFont(new Font(FONT_NAME, Font.BOLD, 15));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topActions.setOpaque(false);
        JButton statsBtn = createButton("WEEKLY SUMMARY", Color.WHITE, TEXT_PRIMARY);
        JButton logoutBtn = createButton("LOGOUT", Color.WHITE, TEXT_PRIMARY);
        topActions.add(statsBtn);
        topActions.add(logoutBtn);

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(topActions, BorderLayout.EAST);

        // -- SIDEBAR --
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_CARD);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(350, 0));

        // Sidebar Header
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setBackground(BG_CARD);
        sideHeader.setBorder(new EmptyBorder(15, 10, 10, 10));

        searchField = createTextField();
        searchField.setText("Search or YYYY-MM-DD");
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().contains("Search")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search or YYYY-MM-DD");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        sideHeader.add(searchField, BorderLayout.CENTER);

        // Lists
        JPanel listsContainer = new JPanel();
        listsContainer.setLayout(new BoxLayout(listsContainer, BoxLayout.Y_AXIS));
        listsContainer.setBackground(BG_CARD);

        JLabel weekLabel = new JLabel("THIS WEEK");
        weekLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        weekLabel.setForeground(Color.GRAY);
        weekLabel.setBorder(new EmptyBorder(15, 15, 5, 15));
        weekLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        weekListModel = new DefaultListModel<>();
        weekList = new JList<>(weekListModel);
        styleList(weekList);

        JLabel historyLabel = new JLabel("ARCHIVES");
        historyLabel.setFont(new Font(FONT_NAME, Font.BOLD, 11));
        historyLabel.setForeground(Color.GRAY);
        historyLabel.setBorder(new EmptyBorder(20, 15, 5, 15));
        historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        historyListModel = new DefaultListModel<>();
        historyList = new JList<>(historyListModel);
        styleList(historyList);

        listsContainer.add(weekLabel);
        listsContainer.add(weekList);
        listsContainer.add(historyLabel);
        listsContainer.add(historyList);

        JScrollPane sidebarScroll = new JScrollPane(listsContainer);
        sidebarScroll.setBorder(null);

        sidebar.add(sideHeader, BorderLayout.NORTH);
        sidebar.add(sidebarScroll, BorderLayout.CENTER);

        // -- EDITOR --
        JPanel editorWrap = new JPanel(new BorderLayout());
        editorWrap.setBackground(BG_MAIN);
        editorWrap.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        weatherLabel = new JLabel("Weather: -");
        weatherLabel.setFont(FONT_BOLD);
        weatherLabel.setForeground(TEXT_PRIMARY);

        moodLabel = new JLabel("Mood: -");
        moodLabel.setFont(FONT_BOLD);
        moodLabel.setForeground(TEXT_PRIMARY);

        infoPanel.add(weatherLabel);
        infoPanel.add(moodLabel);

        journalContentArea = new JTextArea();
        journalContentArea.setFont(new Font(FONT_NAME, Font.PLAIN, 15));
        journalContentArea.setForeground(TEXT_PRIMARY);
        journalContentArea.setLineWrap(true);
        journalContentArea.setWrapStyleWord(true);
        journalContentArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane textScroll = new JScrollPane(journalContentArea);
        textScroll.setBorder(new LineBorder(BORDER_COLOR));

        // BUTTON PANEL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        deleteBtn = createButton("DELETE", new Color(255, 235, 235), Color.RED); // Red Button
        deleteBtn.setBorder(new LineBorder(new Color(255, 200, 200)));
        deleteBtn.setPreferredSize(new Dimension(100, 45));

        saveBtn = createButton("SAVE ENTRY", new Color(230, 230, 230), TEXT_PRIMARY);
        saveBtn.setPreferredSize(new Dimension(150, 45));

        btnPanel.add(deleteBtn);
        btnPanel.add(saveBtn);

        editorWrap.add(infoPanel, BorderLayout.NORTH);
        editorWrap.add(textScroll, BorderLayout.CENTER);
        editorWrap.add(btnPanel, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(sidebar, BorderLayout.WEST);
        panel.add(editorWrap, BorderLayout.CENTER);

        // -- LOGIC --

        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty() || query.contains("Search")) {
                refreshDashboard();
            } else {
                if (isValidDate(query)) openSpecificDate(query);
                else performSearch(query);
            }
        });

        // List Selection
        weekList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !weekList.isSelectionEmpty()) {
                historyList.clearSelection();
                loadSelectedDate(weekList.getSelectedValue());
            }
        });

        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !historyList.isSelectionEmpty()) {
                weekList.clearSelection();
                loadSelectedDate(historyList.getSelectedValue());
            }
        });

        // Save Button
        saveBtn.addActionListener(e -> {
            String selected = weekList.getSelectedValue();
            if (selected == null) selected = historyList.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a date first!");
                return;
            }

            String date = selected.split(" ")[0];
            String content = journalContentArea.getText();

            saveBtn.setText("ANALYZING...");
            saveBtn.setEnabled(false);

            new Thread(() -> {
                String mood = JournalLogic.getMood(content);
                String weather = JournalLogic.getWeather();
                JournalEntry exist = JournalLogic.getEntryForDate(date);

                if (exist == null) JournalLogic.saveEntry(date, content, mood, weather);
                else {
                    exist.setContent(content);
                    exist.setMood(mood);
                    JournalLogic.updateEntry(exist);
                }

                SwingUtilities.invokeLater(() -> {
                    saveBtn.setText("SAVE ENTRY");
                    saveBtn.setEnabled(true);
                    moodLabel.setText("Mood: " + mood);
                    weatherLabel.setText("Weather: " + (exist == null ? weather : exist.getWeather()));

                    refreshDashboard();
                    JOptionPane.showMessageDialog(this, "Saved!");
                });
            }).start();
        });

        // Delete Button Logic
        deleteBtn.addActionListener(e -> {
            String selected = weekList.getSelectedValue();
            if (selected == null) selected = historyList.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select an entry to delete.");
                return;
            }

            String date = selected.split(" ")[0];
            // Check if it actually exists (has checkmark)
            if (!selected.contains("✔")) {
                JOptionPane.showMessageDialog(this, "Nothing to delete (Entry is empty).");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the entry for " + date + "?\nThis cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                JournalLogic.deleteEntry(date);
                refreshDashboard();
                journalContentArea.setText("");
                weatherLabel.setText("Weather: -");
                moodLabel.setText("Mood: -");
                JOptionPane.showMessageDialog(this, "Entry Deleted.");
            }
        });

        logoutBtn.addActionListener(e -> {
            JournalLogic.currentUser = null;
            cardLayout.show(mainContainer, "LOGIN");
        });

        statsBtn.addActionListener(e -> showStats());

        return panel;
    }

    // --- HELPERS (UI Factory) ---

    private void styleList(JList<String> list) {
        list.setFixedCellHeight(35);
        list.setFont(FONT_PLAIN);
        list.setForeground(TEXT_PRIMARY);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(new EmptyBorder(5, 15, 5, 15));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    private JTextField createTextField() {
        JTextField f = new JTextField(15);
        f.setFont(FONT_PLAIN);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    private JPasswordField createPasswordField() {
        JPasswordField f = new JPasswordField(15);
        f.setFont(FONT_PLAIN);
        f.setForeground(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return f;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorder(new EmptyBorder(10, 25, 10, 25));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createFlatButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_LINK);
        b.setForeground(TEXT_PRIMARY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // --- LOGIC HELPERS ---

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void refreshDashboard() {
        if (JournalLogic.currentUser != null) {
            welcomeLabel.setText("Welcome, " + JournalLogic.currentUser.getDisplayName());
        }
        weekListModel.clear();
        historyListModel.clear();

        LocalDate today = LocalDate.now();

        TreeSet<String> allUserDates = new TreeSet<>(Collections.reverseOrder());
        for (JournalEntry entry : JournalLogic.journals) {
            if (entry.getEmail().equals(JournalLogic.currentUser.getEmail())) {
                allUserDates.add(entry.getDate());
            }
        }

        for (int i = 0; i < 7; i++) {
            String date = today.minusDays(i).toString();
            JournalEntry entry = JournalLogic.getEntryForDate(date);
            String marker = (entry != null) ? "  ✔" : "";
            weekListModel.addElement(date + marker);
            allUserDates.remove(date);
        }

        if (allUserDates.isEmpty()) {
            historyListModel.addElement("No older entries");
            historyList.setEnabled(false);
        } else {
            historyList.setEnabled(true);
            for (String date : allUserDates) {
                JournalEntry entry = JournalLogic.getEntryForDate(date);
                String marker = (entry != null) ? "  ✔" : "";
                historyListModel.addElement(date + marker);
            }
        }

        weekList.setSelectedIndex(0);
    }

    private void performSearch(String keyword) {
        List<String> results = JournalLogic.searchJournals(keyword);
        weekListModel.clear();
        historyListModel.clear();

        if (results.isEmpty()) {
            weekListModel.addElement("No matches found.");
            weekList.setEnabled(false);
        } else {
            weekList.setEnabled(true);
            for (String date : results) {
                weekListModel.addElement(date + "  ✔");
            }
        }
        historyListModel.addElement("Search Mode Active");
        historyList.setEnabled(false);
    }

    private void openSpecificDate(String date) {
        weekListModel.clear();
        historyListModel.clear();
        weekList.setEnabled(true);
        JournalEntry entry = JournalLogic.getEntryForDate(date);
        String marker = (entry != null) ? "  ✔" : "";
        weekListModel.addElement(date + marker);
        weekList.setSelectedIndex(0);
        loadSelectedDate(date + marker);
    }

    private void loadSelectedDate(String selected) {
        if (selected == null || selected.contains("No older") || selected.contains("No matches")) return;

        String date = selected.split(" ")[0];
        JournalEntry entry = JournalLogic.getEntryForDate(date);

        if (entry != null) {
            journalContentArea.setText(entry.getContent());
            weatherLabel.setText("Weather: " + entry.getWeather());
            moodLabel.setText("Mood: " + entry.getMood());
        } else {
            journalContentArea.setText("");
            weatherLabel.setText("Weather: -");
            moodLabel.setText("Mood: -");
        }
    }

    private void showStats() {
        String[] cols = {"Date", "Mood", "Weather"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            String date = today.minusDays(i).toString();
            JournalEntry entry = JournalLogic.getEntryForDate(date);
            if (entry != null) model.addRow(new Object[]{date, entry.getMood(), entry.getWeather()});
            else model.addRow(new Object[]{date, "-", "-"});
        }
        JTable table = new JTable(model);
        table.setFont(FONT_PLAIN);
        table.setRowHeight(25);
        table.getTableHeader().setFont(FONT_BOLD);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Weekly Summary", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(SmartJournalGUI::new);
    }
}