import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class App {

    private static Scanner scanner = new Scanner(System.in);
    private static DBHandler dbHandler = new DBHandler("jdbc:sqlite:quizApp.db");

    public static void main(String[] args) {
        System.out.println("Welcome to the Quiz App!");
        

        while (true) {
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                
            } catch (Exception e) {
                System.out.println("Please enter a valid option.");
                continue;
            }

            if (choice == 1) {
                loginUser();
                clearScreen();
            } else if (choice == 2) {
                signUpUser();
                clearScreen();
            } else if (choice == 3) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid option!");
            }
        }

        scanner.close();
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = dbHandler.readUser(username);
        if (user != null && user.login(password)) {
            clearScreen();
            System.out.println("Login successful!");
            handleUserMenu(user);
            
        } else {
            clearScreen();
            System.out.println("Invalid credentials!");
        }
    }

    private static void signUpUser() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();

        dbHandler.createUser(username, password, 0, "Newbie");
        clearScreen();
        System.out.println("User registered!");
        
    }

    private static void handleUserMenu(User user) {
        while (true) {
            System.out.println("1. Take a quiz");
            System.out.println("2. View my scores");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                //clear terminal after input
                clearScreen();
            } catch (Exception e) {
                System.out.println("Please enter a valid option.");
                continue;
            }

            if (choice == 1) {
                takeQuiz(user);
            } else if (choice == 2) {
                viewScores(user);
            } else if (choice == 3) {
                System.out.println("Logged out. Goodbye!");
                break;
            } else {
                System.out.println("Invalid option!");
            }
        }
    }

    private static void takeQuiz(User user) {
        dbHandler.createQuizIfNotExist();
        List<Question> questions = dbHandler.readQuiz(1);
             
        if(questions.isEmpty()) {
            System.out.println("No questions found for the quiz.");
            return;
         }
        
        int correctAnswers = 0;
    
        for (Question q : questions) {
            System.out.println(q.getQuestionText());
            List<Option> options = q.getOptions();
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + q.getOptions().get(i));
            }
            String answer = scanner.nextLine();
    
            if (q.isCorrect(Integer.parseInt(answer))) {
                correctAnswers++;
            }
        }
    
        int score = correctAnswers * 10;
        double percentage = (double) correctAnswers / questions.size() * 100;
    
        if (percentage > 70) {
            score += 100;
        }
    
        user.addPoints(score);
        dbHandler.updateUserPoints(user);
        clearScreen();
        System.out.println("Quiz completed! Your score: " + percentage + "%");
        System.out.println("Total points awarded: " + score + " points");
    }

    public static void clearScreen() {  
        try {  
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }  
        } catch (IOException | InterruptedException ex) {}  
    }


    private static void viewScores(User user) {
        // Logic for viewing scores goes here
    }
}

