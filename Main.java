package paitent_scheduler;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import paitent_scheduler.Scheduler.Appointment;

import java.awt.*;
import java.awt.ScrollPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;


public class Main extends Application{
    private Stage primaryStage;
    private Scene currScene, loginScene, homeScene;
    private TextField usernameTF, pwTF, searchTF;
    private Button loginBtn, nextDay, prevDay, viewCalendar, viewPatient, viewDoctor, viewEmployee, viewAppointment, addPatient, addDoctor, addEmployee, editPatient, editEmployee, deletePatient, deleteEmployee, removeDoctor;
    private boolean isReceptionist;
    private LocalDateTime today = LocalDateTime.now(), currentDay;
    BorderPane home, appointmentView, databaseView;
	final String dbUrl = "jdbc:ucanaccess://src//SchedulerDB.accdb";


    @Override
    public void start(Stage ps) throws Exception {
        currentDay = today;
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        createLoginScene();
        createHomeScene();
        //createTestHomeScene();
        //checks employeeID(username) and password; sets session for read or write

        loginBtn.setOnAction(event -> {
            currScene = homeScene;
            primaryStage.setScene(currScene);
        });

        //change appointment view date to next
        nextDay.setOnAction(event -> {
            currentDay = currentDay.plusDays(1);
            //date.setText(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() +", "+ currentDay.getYear());
            appointmentView.setCenter(createCalendarDay());
            prevDay.setDisable(false);
        });

        prevDay.setOnAction(event -> {
            currentDay = currentDay.minusDays(1);
            //date.setText(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() +", "+ currentDay.getYear());
            appointmentView.setCenter(createCalendarDay());
            if(currentDay.getDayOfYear() == today.getDayOfYear() && currentDay.getYear() == today.getYear())
                prevDay.setDisable(true);
        });
        primaryStage.setScene(createLoginScene());
        primaryStage.show();
    }

    /**
     * builds login scene, checks for reception or non-reception login session
     */
    public Scene createLoginScene(){
        Label enterLoginInfoLb = new Label("Enter Login Credentials");
        Label userLb = new Label("Username: ");
        Label pwLb = new Label("Password: ");
       
        ChoiceBox<String> accountTypeDrop = new ChoiceBox<>();
        accountTypeDrop.getItems().addAll("Receptionist", "Doctor", "Medical Employee");
        
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
        VBox vbLogin = new VBox(20, enterLoginInfoLb, gpLogin, accountTypeDrop, loginBtn);
        vbLogin.setAlignment(Pos.CENTER);
        loginBtn.setOnAction(e -> handle(accountTypeDrop));
        return new Scene(vbLogin, 475, 375);
    }
    /*
     * @param takes in the drop down menu option
     * Desc: when user presses login button, the application will make connection to database to confirm credentials
     */
    public void handle (ChoiceBox<String> accountTypeDrop ) { // when the button is clicked
    		try { 
    			String credential = accountTypeDrop.getValue();	// getting the option the user chose in dropdown
    			String IDquery = null;
    			String passQuery = null;
    			if(credential=="Receptionist") {				// changes values for obtaining login from msaccess database
    				IDquery = "Receptionist_ID=?";
    				passQuery = "R_Password=?";
    			}
    			else if (credential=="Doctor") {
    				IDquery = "Doctor_ID=?";
    				passQuery = "D_Password=?";
    			}
    			else if (credential=="Medical Employee") {
    				credential = "MedicalEmployee";
    				IDquery = "Med_Employee_ID=?";
    				passQuery = "ME_Password=?";
    			}
    			int ID = Integer.parseInt(usernameTF.getText());  // get the ID number from the input
    			String pword = pwTF.getText();
    			Connection con = DriverManager.getConnection(dbUrl);
				PreparedStatement pst = con.prepareStatement("Select * FROM " +credential+ " WHERE " +IDquery+ " AND " +passQuery);
				pst.setInt(1, ID);
				pst.setString(2,pword);
				ResultSet rs = pst.executeQuery();
				if (rs.next()) {			
					/*
					 * ADD if statements to open specific schedules for different users; for example if it is doctor, open the 
					 * schedule so that it is the correct schedule for the role of the doctor to view. Doctors only get 
					 * to view the schedule. EX: primaryStage.setScene(scheduleDisplayDoctor)
					 */
					primaryStage.setScene(homeScene);
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

    /**
     * creates home scene which includes: functions, appointment view, database view
     */
    public void createHomeScene(){
        home = new BorderPane();
        home.setLeft(createFunctionButtonGrid());
        createAppointmentView();
        home.setCenter(appointmentView);
        homeScene = new Scene(home,800,600);
    }

    /**
     * takes in appointments for current day and builds day view
     */
    public void createAppointmentView(){
        //set title
        appointmentView = new BorderPane();
        appointmentView.setPrefSize(500,600);
        Label heading = new Label("Calendar");
        appointmentView.setTop(heading);

        //set appointment view change functions
        nextDay = new Button(">");
        prevDay = new Button("<");
        prevDay.setDisable(true);
        HBox toggleDay = new HBox();
        toggleDay.setAlignment(Pos.CENTER);
        toggleDay.getChildren().addAll(prevDay,nextDay);
        appointmentView.setBottom(toggleDay);
        appointmentView.setCenter(createCalendarDay());
    }

    /**
     *
     * @return gridpane layout with header(date of day) and two columns: timestamp-appointmentID
     */
    public BorderPane createCalendarDay(){
        BorderPane day = new BorderPane();
        day.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");
        Label date = new Label(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() +", "+ currentDay.getYear());
        date.setFont(Font.font("Arial",16));
        day.setTop(date);


		ArrayList<Appointment> result_appt = new ArrayList<Appointment>();
        String resultString = "no appointments";
        java.sql.Date currentDayFormatted = java.sql.Date.valueOf(currentDay.toLocalDate());
        
        Connection con;
		try {
			con = DriverManager.getConnection(dbUrl);
			PreparedStatement pst = con.prepareStatement("Select * FROM Appointment WHERE Appt_Date = '" + currentDayFormatted+ "'");
			ResultSet result = pst.executeQuery();  
			while(result.next()) {
				//result_appt.add( result.getObject(1), Appointment);
			}
        	//resultString = result_appt.get(0).toString();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        GridPane times = new GridPane();
        times.setPadding(new Insets(20,0,0,0));
        times.setVgap(20);
        times.addRow(0, new Label("Times"), new Label("Appointment"));
        times.addRow(1, new Label("7am"), new Label(resultString));
        times.addRow(2, new Label("8am"), new Label(resultString));
        times.addRow(3, new Label("9am"), new Label(resultString));
        times.addRow(4, new Label("10am"), new Label(resultString));
        times.addRow(5, new Label("11am"), new Label(resultString));
        times.addRow(6, new Label("12pm"), new Label(resultString));
        times.addRow(7, new Label("1pm"), new Label(resultString));
        times.addRow(8, new Label("2pm"), new Label(resultString));
        times.addRow(9, new Label("3pm"), new Label(resultString));
        times.addRow(10, new Label("4pm"), new Label(resultString));
        times.addRow(11, new Label("5pm"), new Label(resultString));

        times.setStyle("-fx-font: 12px Arial");

        day.setCenter(times);
        return day;

    }

    /**
     * general functions: view Patient, Doctor, Appointment, Employee
     * reception functions:
     *  - add/edit/remove Patient, Doctor, Employee, Appointment
     *  - check Availability
     */
    public VBox createFunctionButtonGrid(){
        VBox functions = new VBox();
        functions.setPrefSize(300, 600);
        functions.setPadding(new Insets(5,0,5,0));
        functions.setStyle("-fx-background-color: DAE6F3;");
        Label funcHeading = new Label("Functions");
        functions.getChildren().add(funcHeading);

        viewPatient = new Button("View Patient Info.");
        viewDoctor = new Button("View Doctor Info.");
        viewEmployee = new Button("View Employee Info.");
        viewAppointment = new Button("View Appointment Details");
        viewCalendar = new Button("View Calendar");
        viewCalendar.setDisable(true);

        functions.getChildren().addAll(viewPatient, viewDoctor, viewEmployee, viewAppointment, viewCalendar);

        addPatient = new Button("Add Patient");
        addDoctor = new Button("Add Doctor");
        addEmployee = new Button("Add Employee");
        editPatient = new Button("Edit Patient");
        editEmployee = new Button("Edit Employee");
        deleteEmployee = new Button("Delete Employee");
        deletePatient = new Button("Delete Patient");
        removeDoctor = new Button("Delete Doctor");

        if(isReceptionist){
            functions.getChildren().addAll(addPatient,addDoctor,addEmployee,deletePatient,deleteEmployee, removeDoctor, editEmployee, editPatient);
        }
        return functions;

    }


    public void createDatabaseView(String viewType){
        databaseView = new BorderPane();
        //search bar (top)
        HBox searchBar = new HBox();
        Label enterID = new Label(viewType + ": ");
        searchTF = new TextField();
        searchBar.getChildren().addAll(enterID, searchTF);

        GridPane personnelInfo = new GridPane();
        personnelInfo.addRow(0, new Label("ID"), new Label("First Name"), new Label("Last Name"));
        databaseView.setTop(searchBar);
        databaseView.setCenter(personnelInfo);

    }





    /**
     * Text listener for login fields; enables Enter button when username and password field not empty
     */
    private class LoginTextFieldListener implements ChangeListener<String>{
        @Override
        public void changed(ObservableValue<? extends String> source, String oldValue, String newValue) {
            String usernameVal = usernameTF.getText();
            String passwordVal = pwTF.getText();
            loginBtn.setDisable(usernameVal.trim().equals("") || passwordVal.trim().equals(""));
        }
    }




    public static void main(String[] args) {
        launch(args);
    }
}