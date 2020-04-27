package patient_scheduler;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.event.EventHandler;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class TestMain extends Application {
    private Stage primaryStage;
    private String sessionType;
    private boolean isReceptionist;
    private Scene homeScene;
    private BorderPane homeLayout, dataLayout, optionLayout;
    private ToggleButton apptViewTBtn, patientTBtn, employeeTBtn, dayViewTBtn, doctorTBtn, doctorApptTBtn;
    private Button addBtn, editBtn, delBtn, searchBtn, checkAvailBtn, changeDateBtn, nextDayBtn, prevDayBtn;
    private Button loginBtn, logoutBtn;
    private LocalDateTime today = LocalDateTime.now(), currentDay;
    private TextField usernameTF, pwTF, idTF;
    final String dbUrl = "jdbc:ucanaccess://src//SchedulerDB.accdb";
    final ToggleGroup dataViews = new ToggleGroup();
    private String currDV;
    private Scheduler scheduler;

    @Override
    public void start(Stage ps) throws Exception {
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        primaryStage.setScene(createLoginScene());
        primaryStage.show();

        //set button actions for layout toggle buttons
        dataViews.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                //if no button is toggled, set day view
                if(dataViews.getSelectedToggle() == null){
                    dayViewTBtn.setSelected(true);
                }
                optionLayout.setCenter(setDataOptions());
            }
        });


    }


    // check session type and build home scene: functionsLayout + dataLayout
    public void setHomeScene(String credential) {
        currentDay = today;
        scheduler = new Scheduler();
        sessionType = credential;
        if(credential == "Receptionist"){ isReceptionist=true;}
        homeLayout = new BorderPane();
        homeLayout.setPrefSize(800,600);
        //set optionsLayout
        optionLayout = new BorderPane();
        optionLayout.setPrefSize(150,600);
        logoutBtn = new Button("Log Out");
        optionLayout.setBottom(logoutBtn);
        optionLayout.setTop(setLayoutOption());
        optionLayout.setCenter(setDataOptions());
        //set dataLayout
        dataLayout = new BorderPane();
        dataLayout.setPrefSize(650, 600);
        dataLayout.setTop(setDataLayoutHeader());
        dataLayout.setBottom(setDataLayoutBottom());
        dataLayout.setCenter(setDataLayoutCenter());

        homeLayout.setLeft(optionLayout);
        homeLayout.setRight(dataLayout);
        homeScene = new Scene(homeLayout, 800, 600);
    }

    public VBox setDataOptions(){
        VBox options = new VBox();
        Label header = new Label("Options");
        options.getChildren().add(header);

        if(patientTBtn.isSelected() || employeeTBtn.isSelected() || doctorTBtn.isSelected()){
            if(isReceptionist){
                addBtn = new Button("Add");
                editBtn = new Button("Edit");
                delBtn = new Button("Delete");
                options.getChildren().addAll(addBtn, editBtn, delBtn);
            }
        }else if(dayViewTBtn.isSelected()){
            if(isReceptionist){
                addBtn = new Button("Schedule Appointment");
                checkAvailBtn = new Button("Check Availability");
                delBtn = new Button("Delete Appointment");
                delBtn.setDisable(true);
                options.getChildren().addAll(addBtn, checkAvailBtn, delBtn);
            }
            searchBtn = new Button("Find Appointment");
            options.getChildren().add(searchBtn);
        }

        return options;
    }

    //called once when home scene built
    public VBox setLayoutOption(){
        VBox options = new VBox();
        //set toggle group
        Label header = new Label("Data View");
        patientTBtn = new ToggleButton("Patient Records");
        patientTBtn.setOnAction(updateCurrDV);
        patientTBtn.setToggleGroup(dataViews);
        employeeTBtn = new ToggleButton("Employee Records");
        employeeTBtn.setOnAction(updateCurrDV);
        employeeTBtn.setToggleGroup(dataViews);
        doctorTBtn = new ToggleButton("Doctor Records");
        doctorTBtn.setOnAction(updateCurrDV);
        doctorTBtn.setToggleGroup(dataViews);
        apptViewTBtn = new ToggleButton("Appointment Records");
        apptViewTBtn.setOnAction(updateCurrDV);
        apptViewTBtn.setToggleGroup(dataViews);
        dayViewTBtn = new ToggleButton("Day View");
        dayViewTBtn.setOnAction(updateCurrDV);
        dayViewTBtn.setToggleGroup(dataViews);
        //default on start up
        dayViewTBtn.setSelected(true);
        currDV = "Day View";



        if(sessionType == "Doctor"){
            doctorApptTBtn = new ToggleButton("View Appointments");
            options.getChildren().addAll(header, dayViewTBtn,patientTBtn, doctorApptTBtn);
        }else{
            options.getChildren().addAll(header,dayViewTBtn, patientTBtn, employeeTBtn, doctorTBtn);
            if(sessionType == "Receptionist")
                options.getChildren().add(apptViewTBtn);
        }
        return options;
    }

    //changes dataLayout to match selected toggle button
    public Label setDataLayoutHeader(){
        //contains header
        Label header;
        String text;
        if(patientTBtn.isSelected()){
            text = "Patient Records";

        }else if(employeeTBtn.isSelected()){
            text = ("Employee Records");

        }else if(doctorTBtn.isSelected()){
            text = ("Patient Records");

        }else if(dayViewTBtn.isSelected()){
            text = ("Day View: " + currentDay.getMonth() + " " + currentDay.getDayOfMonth() + ", " + currentDay.getYear());

        }else if(apptViewTBtn.isSelected()){
            text = ("Appointment Records");
        }else{
            //doctorAppt
            text = ("Doctor's Appointments");
        }
        header = new Label(text);
        header.setPrefSize(650, 50);
        header.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: green;");
        return header;
    }

    public VBox setDataLayoutCenter(){
        VBox center = new VBox();
        center.setPrefSize(650, 500);
        //3 types: Record, DayView, Appointments
        if(patientTBtn.isSelected()){

        }else if(employeeTBtn.isSelected()){

        }else if(doctorTBtn.isSelected()){

        }else if(dayViewTBtn.isSelected()){

        }else if(apptViewTBtn.isSelected()){

        }else{

        }
        center.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: black;");
        return center;
    }

    public HBox setDataLayoutBottom(){
        HBox searchBar = new HBox();
        //temp
        searchBar.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");
        searchBar.setPrefSize(500, 50);
        Label enterID = new Label("Enter ID: ");
        idTF = new TextField();
        searchBtn = new Button("Search");
        if(currDV=="Day View"){
            //only day view searches by drop down
            nextDayBtn = new Button(">");
            prevDayBtn = new Button("<");
            searchBar.getChildren().addAll(prevDayBtn, nextDayBtn);
            searchBar.setAlignment(Pos.CENTER);
        }else{
            searchBar.getChildren().addAll(enterID, idTF, searchBtn);
        }
        return searchBar;
    }

    public void updateDataLayout(){
        dataLayout.setTop(setDataLayoutHeader());
        dataLayout.setCenter(setDataLayoutCenter());
        dataLayout.setBottom(setDataLayoutBottom());
    }

    /**
     * gets table attributes by selected Table; fills with data from Scheduler
     * @return view of Records Table- patient, employee, doctor OR apptView, doctorAppt
     */
    public void buildRecordView(){
        HBox row = new HBox();
        row.setPrefSize(650, 500);
        String[] attributes;
        Object[] values;
        switch(currDV){
            case "Patient Records":
                attributes = scheduler.getPatientFields();
                values = scheduler.getAppointments(currentDay);

            case "Employee Records":
                attributes = scheduler.getEmployeeFields();

            case "Doctor Records":
                attributes = scheduler.getEmployeeFields();

            case "Appointment Records":
                attributes = new String[]{"Date", "Appointment"};

            case "Doctor Appointments":
                attributes = new String[]{"Date", "Assignment"};

        }
    }

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
        loginBtn.setDisable(true);

        GridPane gpLogin = new GridPane();
        gpLogin.addRow(0, userLb, usernameTF);
        gpLogin.addRow(1, pwLb, pwTF);
        gpLogin.setVgap(10);
        gpLogin.setHgap(10);
        gpLogin.setAlignment(Pos.CENTER);
        VBox vbLogin = new VBox(20, enterLoginInfoLb, gpLogin, accountTypeDrop, loginBtn);
        vbLogin.setAlignment(Pos.CENTER);
        //loginBtn.setOnAction(e -> handle(accountTypeDrop));
        loginBtn.setOnAction(event -> {
            setHomeScene("Receptionist");
            primaryStage.setScene(homeScene);
        });
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
            ((PreparedStatement) pst).setInt(1, ID);
            pst.setString(2,pword);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                /*
                 * ADD if statements to open specific schedules for different users; for example if it is doctor, open the
                 * schedule so that it is the correct schedule for the role of the doctor to view. Doctors only get
                 * to view the schedule. EX: primaryStage.setScene(scheduleDisplayDoctor)
                 */
                setHomeScene(credential);
                primaryStage.setScene(homeScene);
            }
            else {
                Alert wrongCred = new Alert(Alert.AlertType.ERROR);
                wrongCred.setHeaderText("Invalid username or password");
                wrongCred.setContentText("Please contact your administrator if you forgot your credentials");
                wrongCred.showAndWait();
            }
        } catch (Exception e) {
            System.out.println("Connection to database failed");
            Alert invalidUser = new Alert(Alert.AlertType.ERROR);
            invalidUser.setHeaderText("Invalid username or password");
            invalidUser.setContentText("Please contact your administrator if you forgot your credentials");
            invalidUser.showAndWait();
        }
    }

    EventHandler<ActionEvent> updateCurrDV = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Toggle selected = dataViews.getSelectedToggle();
            if(patientTBtn.isSelected()){
                currDV = "Patient Records";
            }else if(employeeTBtn.isSelected()){
                currDV = "Employee Records";
            }else if(doctorTBtn.isSelected()){
                currDV = "Doctor Records";
            }else if(dayViewTBtn.isSelected()){
                currDV = "Day View";
            }else if(apptViewTBtn.isSelected()){
                currDV = "Appointment Records";
            }else{
                //doctor appointments
                currDV = "Doctor Appointments";
            }
            updateDataLayout();
        }
    };

    /**
     * Text listener for login fields; enables Enter button when username and password field not empty
     */
    private class LoginTextFieldListener implements ChangeListener<String> {
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
