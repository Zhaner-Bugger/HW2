
package application;
import javafx.stage.Stage;
import databasePart1.*;

public class NavigationHelper{
	public static void goToHomePage(String role, Stage primaryStage, DatabaseHelper databaseHelper, User currentUser ) {
		System.out.println("Navigating to " + role + "homepage");

		if(role.equals("admin")) {
    		new AdminHomePage(databaseHelper,currentUser).show(primaryStage);
	}else if (role.equals("user")) {
		new UserHomePage(databaseHelper,currentUser).show(primaryStage);
	}else if (role.equals("student")) {
		new StudentHomePage().show(primaryStage);
	}else if (role.equals("instructor")){
		new InstructorHomePage().show(primaryStage);
	}else if (role.equals("staff")) {
		new StaffHomePage().show(primaryStage);
	}else if (role.equals("reviewer")) {
		new ReviewerHomePage().show(primaryStage);
	}

	}
}