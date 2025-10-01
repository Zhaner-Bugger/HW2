package application;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private final StringProperty userName;      // User's userName
    private final StringProperty password;
    private final StringProperty role;
    private List<String>roles = new ArrayList<>();
    private final StringProperty email;
    private final StringProperty name;  // User's real name

    // Constructor to initialize a new User object with userName, password, and role.
    public User( String userName, String password, String email, String name, String role) {
        this.userName = new SimpleStringProperty(userName);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        if(role != null && !role.isEmpty()) {
            this.roles.add(role);

             }
        this.email = new SimpleStringProperty(email);
        this.name = new SimpleStringProperty(name);
    }
    
    // Sets the role of the user.
    public void setRole(String role) {
    	this.role.set(role);;
    }

    public void addRole(String role) {
    	if(role != null && !role.isEmpty() && !roles.contains(role)) {
    		roles.add(role);
    	}
    }
    public String getRole() {
    	return roles.isEmpty() ? null : roles.get(0);
    }
    public List<String> getRoles(){
    	return roles;
    }

    public String getUserName() { return userName.get(); }
    public String getPassword() { return password.get(); }
   
    public String getEmail() { return email.get(); }
    public String getUserInfoName() { return name.get(); }
    
    public StringProperty userNameProperty() {return userName; }
    public StringProperty roleProperty() { 
    	return new SimpleStringProperty(getRole());
    }
    
    public StringProperty allRolesProperty() {
    	String rolesString = String.join(", ", roles);
    	return new SimpleStringProperty(rolesString);
    }
    
}
