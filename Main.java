package application;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class Main extends Application implements EventHandler<ActionEvent> {
    private Stage primaryStage;
    private TextField usernameTF; 
    private PasswordField pwTF;
    private Button loginBtn;
	final String dbUrl = "jdbc:ucanaccess://src//SchedulerDB.accdb";

    @Override
    public void start(Stage ps) throws Exception {
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        Scheduler test = new Scheduler();
        primaryStage.setScene(createLoginScene());
        primaryStage.show();

    }

    public Scene createLoginScene(){
        Label enterLoginInfoLb = new Label("Enter Login Credentials");
        Label userLb = new Label("Username: ");
        Label pwLb = new Label("Password: ");

        LoginTextFieldListener textFieldListener = new LoginTextFieldListener();
        usernameTF = new TextField();
        pwTF = new PasswordField();
        usernameTF.textProperty().addListener(textFieldListener);
        pwTF.textProperty().addListener(textFieldListener);
        loginBtn = new Button("Login");

        GridPane gpLogin = new GridPane();
        gpLogin.addRow(0, userLb, usernameTF);
        gpLogin.addRow(1, pwLb, pwTF);
        gpLogin.setVgap(10);
        gpLogin.setHgap(10);
        gpLogin.setAlignment(Pos.CENTER);
        VBox vbLogin = new VBox(20, enterLoginInfoLb, gpLogin, loginBtn);
        vbLogin.setAlignment(Pos.CENTER);
        loginBtn.setOnAction(this);
        return new Scene(vbLogin, 475, 375);
    }
    
    /*
     * Patient schedule scene displays the weekly schedule
     */
    public Scene scheduleDisplay () {	
    	Label patientSchedule = new Label("Patient Scheduler"); //only for testing
    	VBox layout = new VBox(20, patientSchedule);
    	layout.setAlignment(Pos.CENTER);
    	return new Scene (layout, 600, 500);
    }
    
    

    private class LoginTextFieldListener implements ChangeListener<String>{
        @Override
        public void changed(ObservableValue<? extends String> source, String oldValue, String newValue) {
            String usernameVal = usernameTF.getText();
            String passwordVal = pwTF.getText();
            loginBtn.setDisable(usernameVal.trim().equals("") || passwordVal.trim().equals(""));
        }
    }
    @Override
    /*
     * @param takes in the action that the user performs
     * Desc: whenu user presses login button, the application will make connection to database to confirm credentials
     */
    public void handle (ActionEvent event) { // when the button is clicked
    	if(event.getSource() == loginBtn ) {
    		try { 
    			int ID = Integer.parseInt(usernameTF.getText());
    			String pword = pwTF.getText();
				Connection con = DriverManager.getConnection(dbUrl);
				PreparedStatement pst = con.prepareStatement("Select * FROM Receptionist WHERE Receptionist_ID=? AND R_Password=?");
				pst.setInt(1, ID);
				pst.setString(2,pword);
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {
					primaryStage.setScene(scheduleDisplay());
				} 
				else {
					Alert wrongCred = new Alert(AlertType.ERROR);
					wrongCred.setHeaderText("Invalid username or password");
					wrongCred.setContentText("Please contact your administrator if you forgot your credentials");
					wrongCred.showAndWait();
				}
    		} catch (Exception e) {
    			System.out.println("Connection to database failed");
    			Alert invalidUser = new Alert(AlertType.ERROR);
				invalidUser.setHeaderText("Invalid username or password");
				invalidUser.setContentText("Please contact your administrator if you forgot your credentials");
				invalidUser.showAndWait();
    		}
    	}
    }
    /**
     * @wbp.parser.entryPoint
     */
    public static void main(String[] args) {
        launch(args);
    }
}
