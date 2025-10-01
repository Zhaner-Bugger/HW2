package application;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class HW2Imp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DatabaseHelper db = new DatabaseHelper();

        try {
            db.connectToDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n--- Student Q&A System ---");
            System.out.println("1. Add Question");
            System.out.println("2. List All Questions");
            System.out.println("3. Add Answer to Question");
            System.out.println("4. List Answers for Question");
            System.out.println("5. Mark Answer as Chosen");
            System.out.println("6. List Unresolved Questions");
            System.out.println("7. List Recent Questions");
            System.out.println("8. Add Follow-up Question");
            System.out.println("9. Exit");
            System.out.print("Select: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter your username: ");
                        String userName = sc.nextLine();
                        System.out.print("Enter question text: ");
                        String qText = sc.nextLine();
                        int qId = db.addQuestion(userName, qText);
                        System.out.println("Added question ID: " + qId);
                    }

                    case 2 -> {
                        List<String> allQuestions = db.getAllQuestionsFromDB();
                        System.out.println("\n--- All Questions ---");
                        allQuestions.forEach(System.out::println);
                    }

                    case 3 -> {
                        System.out.print("Enter question ID: ");
                        int qId = sc.nextInt(); sc.nextLine();
                        System.out.print("Enter your username: ");  //Checks for valid user
                        String user = sc.nextLine();
                        System.out.print("Enter answer text: ");
                        String aText = sc.nextLine();
                        int aId = db.addAnswer(qId, user, aText);
                        System.out.println("Added answer ID: " + aId);
                    }

                    case 4 -> {
                        System.out.print("Enter question ID: ");
                        int qId = sc.nextInt(); sc.nextLine();
                        List<Answer> answers = db.getAnswersForQuestion(qId);
                        System.out.println("\n--- Answers ---");
                        for (Answer a : answers) {
                            System.out.println(a);
                            db.markAnswerRead(a.getId()); // mark as read
                        }
                    }

                    case 5 -> {
                        System.out.print("Enter question ID: ");
                        int qId = sc.nextInt(); sc.nextLine();
                        System.out.print("Enter answer ID to mark as chosen: ");
                        int aId = sc.nextInt(); sc.nextLine();
                        if (db.acceptAnswer(qId, aId)) {
                            System.out.println("Marked answer as chosen and question resolved.");
                        } else {
                            System.out.println("Failed to mark answer as chosen.");
                        }
                    }

                    case 6 -> {
                        List<String> unresolved = db.getUnresolvedQuestions();
                        System.out.println("\n--- Unresolved Questions ---");
                        unresolved.forEach(System.out::println);
                    }

                    case 7 -> {
                        List<String> recent = db.getAllQuestionsFromDBOrderedByNewest();
                        System.out.println("\n--- Recent Questions ---");
                        recent.forEach(System.out::println);
                    }

                    case 8 -> {
                        System.out.print("Enter parent question ID: ");
                        int parentId = sc.nextInt(); sc.nextLine();
                        System.out.print("Enter your username: ");
                        String userName = sc.nextLine();
                        System.out.print("Enter follow-up question text: ");
                        String qText = sc.nextLine();
                        int qId = db.addFollowUpQuestion(parentId, userName, qText);
                        System.out.println("Added follow-up question ID: " + qId);
                    }

                    case 9 -> {
                        System.out.println("Goodbye!");
                        db.closeConnection();
                        return;
                    }

                    default -> System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
        }
    }
}