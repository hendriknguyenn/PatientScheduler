package patient_scheduler;

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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.ScrollPane;
import java.time.LocalDateTime;
import java.util.Date;


public class Main extends Application{
    private Stage primaryStage;
    private Scene currScene, loginScene, homeScene;
    private TextField usernameTF, pwTF, searchTF;
    private Button loginBtn, nextDay, prevDay, viewCalendar, viewPatient, viewDoctor, viewEmployee, viewAppointment, addPatient, addDoctor, addEmployee, editPatient, editEmployee, deletePatient, deleteEmployee, removeDoctor;
    private boolean isReceptionist;
    private LocalDateTime today = LocalDateTime.now(), currentDay;
    BorderPane home, appointmentView, databaseView;

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
        primaryStage.setScene(homeScene);
        primaryStage.show();
    }

    /**
     * builds login scene, checks for reception or non-reception login session
     */
    public void createLoginScene(){
        Label heading = new Label("Enter Login Credentials");
        Label userLb = new Label("Employee ID: ");
        Label pwLb = new Label("Password: ");
        usernameTF = new TextField();
        pwTF = new TextField();
        //text listener waits for input in both username and password field, sets Enter button disabled until inputs
        LoginTextFieldListener loginListener = new LoginTextFieldListener();
        usernameTF.textProperty().addListener(loginListener);
        pwTF.textProperty().addListener(loginListener);
        loginBtn = new Button("Enter");
        loginBtn.setDisable(true);
        GridPane gpLogin = new GridPane();
        gpLogin.addRow(0, userLb,usernameTF);
        gpLogin.addRow(1, pwLb, pwTF);
        gpLogin.setVgap(10);
        gpLogin.setHgap(10);
        gpLogin.setAlignment(Pos.CENTER);
        VBox vbLogin = new VBox(20, heading, gpLogin, loginBtn);
        vbLogin.setAlignment(Pos.CENTER);
        loginScene = new Scene(vbLogin, 475, 375);
        //first scene
        currScene = loginScene;
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

        GridPane times = new GridPane();
        times.setPadding(new Insets(20,0,0,0));
        times.setVgap(20);
        times.addRow(0, new Label("Times"), new Label("Appointment"));
        times.addRow(1, new Label("7am"));
        times.addRow(2, new Label("8am"));
        times.addRow(3, new Label("9am"));
        times.addRow(4, new Label("10am"));
        times.addRow(5, new Label("11am"));
        times.addRow(6, new Label("12pm"));
        times.addRow(7, new Label("1pm"));
        times.addRow(8, new Label("2pm"));
        times.addRow(9, new Label("3pm"));
        times.addRow(10, new Label("4pm"));
        times.addRow(11, new Label("5pm"));

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