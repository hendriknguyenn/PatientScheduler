package patient_scheduler;

import com.sun.org.apache.xml.internal.resolver.helpers.BootstrapResolver;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class Main extends Application {
    private Button loginBtn, logoutBtn, addBtn, editBtn, delBtn, nextDayBtn, prevDayBtn, exitBtn, scheduleBtn, cancelBtn;
    private TextField usernameTF, pwTF, searchTF;
    final String dbUrl = "//src//SchedulerDB.accdb";
    private Stage primaryStage, addAppointmentWindow;
    private String sessionType, sessionUserID, currentView;
    private BorderPane home, functions, data;
    private RadioButton dayView, patientDBView, employeeDBView, doctorDBView, appointmentDBView;
    private ToggleGroup viewOptions, dayViewAppointments;
    LocalDateTime today, currentDay;
    private Label date;
    private static TimeSlot selected;

    //for testing
    Scheduler.Patient[] tempPatients = new Scheduler.Patient[]{new Scheduler.Patient("hendrik","nguyen","04/29/1996","123123123","714-732-3525", "Cal Poly Pomona", "hendrikn@cpp.edu"),
    new Scheduler.Patient("john","smith","02/01/1997","123141233","714-732-3525", "UCLA", "johnsmith@pilgrims.com")};
    String[] doctorNames = new String[]{"Jim", "John", "Joe", "Jeff"};
    String[] jimsAppointments = new String[]{"ApptID #11",null,null,null,"ApptID #15",null,"ApptID #8",null,"ApptID #21",null};



    @Override
    public void start(Stage ps) throws Exception {
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        primaryStage.setResizable(false);
        primaryStage.setScene(createLoginScene());
        primaryStage.show();
    }

    /**
     * uses BorderPane home with home.Left = functions and home.Right = dataView
     * @return
     */
    public Scene createHomeScene(){
        home = new BorderPane();
        today = LocalDateTime.now();
        currentDay = today;
        currentView = "Day View";
        setViewOptions();
        setDataOptions();
        home.setLeft(functions);
        setData();
        home.setRight(data);

        return new Scene(home, 800, 600);
    }

    /**
     * called when switching dayview to/from records view
     */
    public void setData(){
        data = new BorderPane();
        data.setPrefSize(600,600);
        if(currentView == "Day View"){
            //top: Day View header + current date
            Label header = new Label("Day View: ");
            header.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 24));
            date = new Label(currentDay.getMonth() + " " + currentDay.getDayOfMonth() + ", " + currentDay.getYear());
            date.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 18));
            HBox dayViewHeader = new HBox(header, date);
            dayViewHeader.setPadding(new Insets(5,0,5,10));
            dayViewHeader.setAlignment(Pos.CENTER_LEFT);
            dayViewHeader.setPrefSize(600, 50);
            dayViewHeader.setStyle("-fx-background-color: #f5fffa");
            data.setTop(dayViewHeader);
            //data.setBottom(search);
            data.setCenter(createDayView());
        }else{
            //database view

        }
        home.setRight(data);
    }

    public BorderPane createDayView(){
        BorderPane dayView = new BorderPane();
        dayView.setPrefSize(600,550);
        //top: searchBar
        HBox searchBar = new HBox();
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setStyle("-fx-padding: 5;" +
                "-fx-background-color: DAE6F3;");
        Label search = new Label("Search: ");
        search.setFont(Font.font("Arial",FontWeight.BOLD,FontPosture.ITALIC,16));
        searchTF = new TextField();
        Button searchBtn = new Button("Search");
        Label searchFormat = new Label("[mm-dd-yyyy]");
        searchBar.getChildren().addAll(search, searchTF, searchFormat,searchBtn);
        searchBar.setSpacing(2);
        dayView.setTop(searchBar);
        //right: nextDayBtn
        nextDayBtn = new Button(">");
        nextDayBtn.setOnAction(event -> {
            currentDay = currentDay.plusDays(1);
            date.setText(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() +", "+ currentDay.getYear());
            data.setCenter(createDayView());
            prevDayBtn.setDisable(false);
        });
        nextDayBtn.setPrefSize(30, 400);
        HBox box = new HBox(nextDayBtn);
        box.setAlignment(Pos.CENTER);
        dayView.setRight(box);
        //left: prevDayBtn
        prevDayBtn = new Button("<");
        prevDayBtn.setOnAction(event -> {
            currentDay = currentDay.minusDays(1);
            date.setText(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() +", "+ currentDay.getYear());
            data.setCenter(createDayView());
            if(currentDay.getDayOfYear() == today.getDayOfYear() && currentDay.getYear() == today.getYear())
                prevDayBtn.setDisable(true);
        });
        prevDayBtn.setPrefSize(30, 400);
        if(currentDay == today)
            prevDayBtn.setDisable(true);
        box = new HBox(prevDayBtn);
        box.setAlignment(Pos.CENTER);
        dayView.setLeft(box);
        //center: dayview appointment data
        dayView.setCenter(getAppointmentData());

        return dayView;
    }

    public BorderPane getAppointmentData(){
        BorderPane appointmentData = new BorderPane();
        int apptHeight = 500;
        appointmentData.setPrefSize(600, apptHeight);
        //left: times
        VBox times = new VBox();
        times.setPrefSize(75, apptHeight-100);
        HBox slot;
        Label header = new Label("Times");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        slot = new HBox(header);
        slot.setPrefSize(20, 50);
        slot.setAlignment(Pos.CENTER);
        times.getChildren().add(slot);
        for(int i=7; i<17; i++){
            String timestamp;
            if(i < 12){
                timestamp = i + "am";
            }else if(i==12){
                timestamp = i +"pm";
            }else{
                timestamp = (i-11) + "pm";
            }
            Label t = new Label(timestamp);
            t.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            slot = new HBox(t);
            slot.setPrefSize(20, (apptHeight-50)/11);
            slot.setAlignment(Pos.CENTER);
            times.getChildren().add(slot);
        }
        appointmentData.setLeft(times);
        appointmentData.setCenter(fillAppointmentData());
        return appointmentData;
    }

    public ScrollPane fillAppointmentData(){
        ScrollPane content = new ScrollPane();
        int apptHeight = 500;
        VBox col;
        HBox data = new HBox();
        dayViewAppointments = new ToggleGroup();
        dayViewAppointments.selectedToggleProperty().addListener(timeSlotSelection);
        //dayViewAppointments.selectedToggleProperty().addListener();
        if(sessionType == "Doctor"){
            //get appointments for that doctor only

        }else{
            //get all appointments for that day, sets ToggleButton userData
            //for each doctor (column header)
            for(int i=0; i<doctorNames.length;i++){
                Label doctor = new Label(doctorNames[i]);
                doctor.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                HBox header = new HBox(doctor);
                header.setPrefSize(50, 50);
                header.setAlignment(Pos.CENTER);
                col = new VBox();
                col.getChildren().add(header);
                ToggleButton timeslot;
                //get appointments for each doctor
                for(int j=0; j<10; j++){
                    //get time for user data
                    String time;
                    if(j<5){
                        time = (j+7) + "am";
                    }else if(j==5){
                        time = (j+7) + "pm";
                    }else{
                        time = (j-5) + "pm";
                    }
                    //temp
                    String buttonText;
                    if(jimsAppointments[j]==null){
                        //empty time slot
                        buttonText = "N/A";
                        timeslot = new ToggleButton("N/A");
                        timeslot.setStyle("-fx-border-style: solid inside;"+
                                "fx-border-width: 1px;"+
                                "fx-border-color: black;");
                        timeslot.setToggleGroup(dayViewAppointments);
                    }else{
                        //taken time slot
                        buttonText = jimsAppointments[j];
                        timeslot = new ToggleButton(jimsAppointments[j]);
                        timeslot.setStyle("-fx-border-style: solid inside;"+
                                "fx-border-width: 1px;"+
                                "fx-border-color: black;");
                        timeslot.setToggleGroup(dayViewAppointments);
                    }
                    timeslot.setUserData(new TimeSlot(buttonText, currentDay.getMonth() + " " +currentDay.getDayOfMonth() + ", " + currentDay.getYear(), time,doctorNames[i]));
                    timeslot.setPrefSize(110,(apptHeight-50)/11);
                    col.getChildren().add(timeslot);
                }
                data.getChildren().add(col);
            }
            content.setContent(data);
            content.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            content.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        }

        return content;
    }

    public void setViewOptions(){
        functions = new BorderPane();
        functions.setPrefSize(200, 600);
        functions.setStyle("-fx-padding: 5;" +
                "-fx-background-color: DAE6F3;");
        VBox view = new VBox();
        view.setSpacing(5);
        view.setPrefSize(200, 200);
        viewOptions = new ToggleGroup();
        Label header = new Label("View Options");
        header.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16));
        dayView = new RadioButton("Day View");
        dayView.setToggleGroup(viewOptions);
        dayView.setSelected(true);
        dayView.setOnAction(changeViewEvent);
        patientDBView = new RadioButton("Patient Records");
        patientDBView.setToggleGroup(viewOptions);
        patientDBView.setOnAction(changeViewEvent);
        employeeDBView = new RadioButton("Employee Records");
        employeeDBView.setToggleGroup(viewOptions);
        employeeDBView.setOnAction(changeViewEvent);
        doctorDBView = new RadioButton("Doctor Records");
        doctorDBView.setToggleGroup(viewOptions);
        doctorDBView.setOnAction(changeViewEvent);
        appointmentDBView = new RadioButton("Appointment Records");
        appointmentDBView.setToggleGroup(viewOptions);
        appointmentDBView.setOnAction(changeViewEvent);
        dayView.setPrefWidth(200);
        patientDBView.setPrefWidth(200);
        employeeDBView.setPrefWidth(200);
        doctorDBView.setPrefWidth(200);
        appointmentDBView.setPrefWidth(200);
        if(sessionType == "Receptionist"){
            view.getChildren().addAll(header, dayView, patientDBView, employeeDBView, doctorDBView, appointmentDBView);
        }else if(sessionType == "Doctor"){
            //doctor can view: dayview of their appointments, patientDB, and appointmentDB of only their appointments
            appointmentDBView = new RadioButton("Your Appointments");
            view.getChildren().addAll(header, dayView, appointmentDBView, patientDBView);
        }else{
            //employee can view: dayview and all appointments
            view.getChildren().addAll(header, dayView, appointmentDBView);
        }
        view.setAlignment(Pos.TOP_CENTER);
        functions.setTop(view);
        //set bottom: logout + exit program
        logoutBtn = new Button("Log Out");
        logoutBtn.setPrefWidth(200);
        logoutBtn.setOnAction(event -> {
            primaryStage.setScene(createLoginScene());
        });
        exitBtn = new Button("Exit Program");
        exitBtn.setPrefWidth(200);
        exitBtn.setOnAction(event -> {
            primaryStage.close();
        });
        VBox bottom = new VBox(logoutBtn,exitBtn);
        functions.setBottom(bottom);
    }

    public void setDataOptions(){
        VBox data = new VBox();
        data.setSpacing(5);
        Label header = new Label("Data Options");
        header.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16));
        if(sessionType == "Receptionist"){
            if(currentView == "Day View" || currentView == "Appointment"){
                addBtn = new Button("Add Appointment");
                addBtn.setDisable(true);
                addBtn.setOnAction(addAppointmentEvent);
                editBtn = new Button("Edit Appointment");
                editBtn.setDisable(true);
                delBtn = new Button("Delete Appointment");
                delBtn.setDisable(true);
                data.getChildren().addAll(header, addBtn, editBtn, delBtn);
            }else if(currentView == "Doctor"){
                addBtn = new Button("Add Doctor");
                addBtn.setDisable(true);
                editBtn = new Button("Edit Doctor");
                editBtn.setDisable(true);
                delBtn = new Button("Delete Doctor");
                delBtn.setDisable(true);
                data.getChildren().addAll(header, addBtn, editBtn, delBtn);
            }else if(currentView == "Employee"){
                addBtn = new Button("Add Employee");
                addBtn.setDisable(true);
                editBtn = new Button("Edit Employee");
                editBtn.setDisable(true);
                delBtn = new Button("Delete Employee");
                delBtn.setDisable(true);
                data.getChildren().addAll(header, addBtn, editBtn, delBtn);
            }else{
                addBtn = new Button("Add Patient");
                addBtn.setDisable(true);
                editBtn = new Button("Edit Patient");
                editBtn.setDisable(true);
                delBtn = new Button("Delete Patient");
                delBtn.setDisable(true);
                data.getChildren().addAll(header, addBtn, editBtn, delBtn);
            }
            addBtn.setPrefWidth(200);
            editBtn.setPrefWidth(200);
            delBtn.setPrefWidth(200);
        }else if(sessionType == "Doctor"){


        }else{

        }
        data.setAlignment(Pos.CENTER);
        functions.setCenter(data);
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
            sessionType = "Receptionist";
            primaryStage.setScene(createHomeScene());
        });
        return new Scene(vbLogin, 475, 375);
    }

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
                sessionUserID = IDquery;
                sessionType = credential;
                primaryStage.setScene(createHomeScene());
            }else {
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

    /**
     * gets appointment button selected from day view; sets add, edit, delete
     * if N/A selected: appointment slot available (add appointment)
     * else: appointment slot taken (edit/del appointment)
     */
    ChangeListener<Toggle> timeSlotSelection = (observable, oldValue, newValue) -> {
        if(newValue == null){
            //unselect a selected time slot
            addBtn.setDisable(true);
            editBtn.setDisable(true);
            delBtn.setDisable(true);
        }else{
            //buttonData: 0-slotText(n/a) 1-date 2-time 3-doctorID
            String[] buttonData = newValue.getUserData().toString().split("-");
            selected = new TimeSlot(buttonData[0], buttonData[1], buttonData[2], buttonData[3]);
            //empty slot selected
            if(selected.isAvailable){
                addBtn.setDisable(false);
                editBtn.setDisable(true);
                delBtn.setDisable(true);
            }else{
                addBtn.setDisable(true);
                editBtn.setDisable(false);
                delBtn.setDisable(false);
            }
        }
    };

    EventHandler<ActionEvent> addAppointmentEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            addAppointmentWindow = new Stage();
            addAppointmentWindow.setTitle("Appointment Scheduler");
            GridPane addApptForm = new GridPane();
            addApptForm.setAlignment(Pos.CENTER);
            addApptForm.setVgap(10);
            addApptForm.setPrefSize(350,400);
            //Header
            HBox header = new HBox();
            Label heading = new Label("Appointment Information");
            heading.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            header.setPrefHeight(50);
            header.getChildren().add(heading);
            addApptForm.addRow(0,header);
            //temp
            //form fields: patientID, doctorID(not selectable), date, time, reason
            //choose patient
            Label patient = new Label("Patient: ");
            patient.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            //fill patient choice box with list of all patients
            ChoiceBox<String> patientList = new ChoiceBox<>();
            String[] listOptions = new String[tempPatients.length];
            for(int i=0;i<listOptions.length;i++){
                listOptions[i] = tempPatients[i].fname + tempPatients[i].lname;
            }
            patientList.getItems().addAll(listOptions);
            HBox pBox = new HBox(patient, patientList);
            addApptForm.addRow(1, pBox);
            //get data from selected Time Slot
            //display date
            Label date = new Label("Date: ");
            date.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            Label selectedDate = new Label(selected.date);
            selectedDate.setFont(Font.font("Arial",FontPosture.ITALIC, 16));
            HBox dBox = new HBox(date, selectedDate);
            dBox.setSpacing(5);
            addApptForm.addRow(2, dBox);
            //display time/duration
            Label time = new Label("Time: ");
            time.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            Label selectedTime = new Label(selected.time);
            selectedTime.setFont(Font.font("Arial",FontPosture.ITALIC, 16));
            HBox tBox = new HBox(time, selectedTime);
            tBox.setSpacing(5);
            addApptForm.addRow(3, tBox);
            //display doctorID
            Label doc = new Label("Doctor: ");
            doc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            Label selectedDoctor = new Label(selected.doctorID);
            selectedDoctor.setFont(Font.font("Arial", FontPosture.ITALIC, 16));
            HBox docBox = new HBox(doc, selectedDoctor);
            docBox.setSpacing(5);
            addApptForm.addRow(4, docBox);
            scheduleBtn = new Button("Schedule");
            cancelBtn = new Button("Cancel");
            cancelBtn.setOnAction(event1 -> {
                addAppointmentWindow.close();
            });
            HBox schedButtons = new HBox(scheduleBtn, cancelBtn);
            schedButtons.setPrefHeight(50);
            addApptForm.addRow(5, schedButtons);
            addAppointmentWindow.setScene(new Scene(addApptForm));
            addAppointmentWindow.showAndWait();
        }
    };

    EventHandler<ActionEvent> changeViewEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(dayView.isSelected()){
                currentView = "Day View";
            }else if(employeeDBView.isSelected()){
                currentView = "Employee";
            }else if(doctorDBView.isSelected()){
                currentView = "Doctor";
            }else if(patientDBView.isSelected()){
                currentView = "Patient";
            }else if(appointmentDBView.isSelected()){
                currentView = "Appointment";
            }else{
                currentView = "Day View";
                dayView.setSelected(true);
            }
            setDataOptions();
        }
    };

    private class TimeSlot{
        String date, time, doctorID, slotText;
        boolean isAvailable;
        private TimeSlot(String text, String d, String t, String doctor){
            slotText = text;
            if(slotText.equals("N/A")){
                isAvailable = true;
            }else{
                isAvailable = false;
            }
            date = d;
            time = t;
            doctorID = doctor;
        }
        public String toString(){
            return slotText + "-" + date + "-" + time + "-" + doctorID;
        }
    }


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
