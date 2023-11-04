import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHandler {
    private Connection connection;
    private String url;

    public DBHandler(String url) {
        this.url = url;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(url);
            Statement stmt = connection.createStatement();

            // Users table
            String sqlUsers = "CREATE TABLE IF NOT EXISTS Users (" +
                              "userID INTEGER PRIMARY KEY," +
                              "username TEXT NOT NULL," +
                              "password TEXT NOT NULL," +
                              "totalPoints INTEGER DEFAULT 0," +
                              "rank TEXT DEFAULT 'Newbie'" +
                              ");";
            stmt.execute(sqlUsers);

            // Quizzes table
            String sqlQuizzes = "CREATE TABLE IF NOT EXISTS Quizzes (" +
                                "quizID INTEGER PRIMARY KEY," +
                                "topic TEXT NOT NULL" +
                                ");";
            stmt.execute(sqlQuizzes);

            // Questions table
            String sqlQuestions = "CREATE TABLE IF NOT EXISTS Questions (" +
                                  "questionID INTEGER PRIMARY KEY," +
                                  "quizID INTEGER," +
                                  "questionText TEXT NOT NULL," +
                                  "correctAnswer INTEGER," +
                                  "FOREIGN KEY(quizID) REFERENCES Quizzes(quizID)" +
                                  ");";
            stmt.execute(sqlQuestions);

            // Options table
            String sqlOptions = "CREATE TABLE IF NOT EXISTS Options (" +
                                "optionID INTEGER PRIMARY KEY," +
                                "questionID INTEGER," +
                                "optionText TEXT NOT NULL," +
                                "FOREIGN KEY(questionID) REFERENCES Questions(questionID)" +
                                ");";
            stmt.execute(sqlOptions);

            // UserQuizzes table
            String sqlUserQuizzes = "CREATE TABLE IF NOT EXISTS UserQuizzes (" +
                                    "userID INTEGER," +
                                    "quizID INTEGER," +
                                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                                    "score INTEGER," +
                                    "FOREIGN KEY(userID) REFERENCES Users(userID)," +
                                    "FOREIGN KEY(quizID) REFERENCES Quizzes(quizID)" +
                                    ");";
            stmt.execute(sqlUserQuizzes);

            stmt.close();
        } catch (Exception e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void executeQuery(String query) {
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createUser(String username, String password, int totalPoints, String rank) {
        String query = "INSERT INTO Users(username, password, totalPoints, rank) VALUES(?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, totalPoints);
            stmt.setString(4, rank);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public User readUser(String username) {
        String query = "SELECT userID, username, password, totalPoints FROM Users WHERE username = ?";
        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("userID"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Question> readQuiz(int quizID) {
        String questionsQuery = "SELECT * FROM Questions WHERE quizID = ?";
        String optionsQuery = "SELECT * FROM Options WHERE questionID = ?";
        
        try (Connection conn = this.connect();
            PreparedStatement questionsStmt = conn.prepareStatement(questionsQuery);
            PreparedStatement optionsStmt = conn.prepareStatement(optionsQuery)) { 
                
            List<Question> questionsForThisQuiz = new ArrayList<>();
                
            questionsStmt.setInt(1, quizID);
            ResultSet rsQuestions = questionsStmt.executeQuery();
                
            while (rsQuestions.next()) {
                int currentQuestionID = rsQuestions.getInt("questionID");
                optionsStmt.setInt(1, currentQuestionID);
                ResultSet rsOptionsForCurrentQuestion = optionsStmt.executeQuery();
                
                List<Option> optionsForCurrentQuestion = new ArrayList<>();
                
                while (rsOptionsForCurrentQuestion.next()) {
                    Option option = new Option(rsOptionsForCurrentQuestion.getInt("optionID"), 
                                            rsOptionsForCurrentQuestion.getInt("questionID"), 
                                            rsOptionsForCurrentQuestion.getString("optionText"));
                    optionsForCurrentQuestion.add(option);
                }
            
                Question question = new Question(currentQuestionID, 
                                                rsQuestions.getString("questionText"), 
                                                optionsForCurrentQuestion,
                                                rsQuestions.getInt("correctAnswer"));
                questionsForThisQuiz.add(question);
            }
            
            return questionsForThisQuiz;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return null;
    }

    public void saveUserQuizScore(User user, int quizID, int score) {
        String query = "INSERT INTO UserQuizzes(userID, quizID, timestamp, score) VALUES(?,?, datetime('now'),?)";
        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, user.getUserID());
            stmt.setInt(2, quizID);
            stmt.setInt(3, score);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateUserPoints(User user) {
        String query = "UPDATE Users SET totalPoints = ? WHERE userID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setInt(1, user.getTotalPoints());
            stmt.setInt(2, user.getUserID());

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                System.out.println("Failed to update points for user: " + user.getUsername());
            } else {
                System.out.println("Points updated successfully for user: " + user.getUsername());
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean doesQuizExist() {
        String checkQuery = "SELECT COUNT(*) FROM Quizzes WHERE topic = 'CompTIA Security + Quiz';";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkQuery);
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // Returns true if the quiz exists
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    
    public void readOptions(){
        String optionsQuery = "SELECT * FROM Options;";
        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
                stmt.execute(optionsQuery);
                ResultSet rs = stmt.executeQuery(optionsQuery);
                while (rs.next()) {
                    System.out.println(Integer.toString(rs.getInt("optionID")) + Integer.toString(rs.getInt("questionID")) + rs.getString("optionText"));
                }
        
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

    }

    public void createQuizIfNotExist() {
        if (!doesQuizExist()) {
        String createQuiz = "INSERT INTO Quizzes (topic) VALUES ('CompTIA Security + Quiz');";
        String insertQuestionQuery = "INSERT INTO Questions (quizID, questionText, correctAnswer) VALUES\r\n" + //
                "(1, 'Which of the following encryption algorithms is a symmetric encryption method?', 2),\r\n" + //
                "(1, 'Which type of attack involves intercepting communication between two entities without altering the communication?', 3),\r\n" + //
                "(1, 'Which of the following is NOT a type of intrusion detection system (IDS)?', 4),\r\n" + //
                "(1, 'The primary purpose of a digital signature is to provide which of the following?', 1),\r\n" + //
                "(1, 'What type of malware typically requires user interaction, such as opening a file or clicking a link, to propagate?', 1),\r\n" + //
                "(1, 'Which protocol provides encryption at the transport layer?', 4),\r\n" + //
                "(1, 'Which of the following best describes the practice of tailgating as it pertains to security?', 3),\r\n" + //
                "(1, 'What does the principle of \"least privilege\" dictate in security?', 1),\r\n" + //
                "(1, 'Which encryption method involves one key for encryption and a different key for decryption?', 2),\r\n" + //
                "(1, 'Which security principle involves having more than one person required to complete a critical task?', 4);\r\n" + //
                "";
        String insertOptionsQuery = "INSERT INTO Options (questionID, optionText) VALUES\r\n" + //
            "(1, 'AES'),\r\n" + //
            "(1, 'RSA'),\r\n" + //
            "(1, 'DH'),\r\n" + //
            "(1, 'SHA-256'),\r\n" + //
            "(2, 'Man-in-the-middle attack'),\r\n" + //
            "(2, 'Brute force attack'),\r\n" + //
            "(2, 'DDoS attack'),\r\n" + //
            "(2, 'Replay attack'),\r\n" + //
            "(3, 'Network-based IDS'),\r\n" + //
            "(3, 'Host-based IDS'),\r\n" + //
            "(3, 'Cloud-based IDS'),\r\n" + //
            "(3, 'Virus-based IDS'),\r\n" + //
            "(4, 'Authentication of a sender'),\r\n" + //
            "(4, 'Ensuring message hasnt been altered'),\r\n" + //
            "(4, 'Encrypting the message content'),\r\n" + //
            "(4, 'Making the message go faster'),\r\n" + //
            "(5, 'Worm'),\r\n" + //
            "(5, 'Trojan'),\r\n" + //
            "(5, 'Rootkit'),\r\n" + //
            "(5, 'Ransomware'),\r\n" + //
            "(6, 'HTTP'),\r\n" + //
            "(6, 'FTP'),\r\n" + //
            "(6, 'SMTP'),\r\n" + //
            "(6, 'TLS'),\r\n" + //
            "(7, 'Sending deceptive emails'),\r\n" + //
            "(7, 'Following someone closely to bypass security'),\r\n" + //
            "(7, 'Using a long, fluffy tail to distract security personnel'),\r\n" + //
            "(7, 'Hacking into a database'),\r\n" + //
            "(8, 'Everyone should have admin rights'),\r\n" + //
            "(8, 'Users should have the minimum necessary access to perform their jobs'),\r\n" + //
            "(8, 'Security is not important'),\r\n" + //
            "(8, 'Everyone should know the root password'),\r\n" + //
            "(9, 'Symmetric encryption'),\r\n" + //
            "(9, 'Hashing'),\r\n" + //
            "(9, 'Asymmetric encryption'),\r\n" + //
            "(9, 'Quantum encryption'),\r\n" + //
            "(10, 'Redundancy'),\r\n" + //
            "(10, 'Failover'),\r\n" + //
            "(10, 'Diversity'),\r\n" + //
            "(10, 'Dual control');\r\n" + //
            "";
        
            try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
                stmt.execute(createQuiz);
                System.out.println("Quiz created successfully!");
                stmt.execute(insertQuestionQuery);
                System.out.println("Questions inserted successfully!");
                stmt.execute(insertOptionsQuery);
                System.out.println("Options inserted successfully!");
        
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Quiz already exists!");
        }
    }
    
}
