package patient_scheduler;

import com.sun.org.apache.xml.internal.resolver.helpers.BootstrapResolver;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.lang.model.element.Element;
import javax.management.remote.rmi._RMIConnection_Stub;
import javax.swing.*;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class Main extends Application {
    private Button loginBtn, logoutBtn, addBtn, editBtn, delBtn, nextDayBtn, prevDayBtn, exitBtn, scheduleBtn, cancelBtn, viewProfileBtn;
    private TextField usernameTF, pwTF, currentPW, searchRecordTF;
    final String dbUrl = "//src//SchedulerDB.accdb";
    private Stage primaryStage, addAppointmentWindow, userProfileWindow, editPatientRecordWindow, deleteRecordWindow, addNonApptWindow;
    private String sessionType, sessionUserID, currentView, selectedRecordID, selectedRecordType;
    private BorderPane home, functions, data;
    private RadioButton dayView, patientDBView, employeeDBView, doctorDBView, appointmentDBView;
    private ToggleGroup viewOptions, dayViewAppointments, dataBaseSelection;
    private LocalDate today, currentDay;
    private Label date;
    private Scheduler dbAccessor;
    //use as reference for: editing a selected record or timeslot
    private Scheduler.Patient selectedPatRec;
    private Scheduler.Employee selectedEmpRec;
    private Scheduler.Doctor selectedDocRec;
    private Scheduler.Appointment selectedAppt;
    private static TimeSlot selected;

    @Override
    public void start(Stage ps) throws Exception {
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        primaryStage.setResizable(false);
        primaryStage.setScene(createLoginScene());
        primaryStage.show();
    }

    public Scene createHomeScene(){
        dbAccessor = new Scheduler();
        home = new BorderPane();
        today = LocalDate.now();
        currentDay = today;
        currentView = "Day View";
        setViewOptions();
        setDataOptions();
        home.setLeft(functions);
        setData();
        home.setRight(data);
        return new Scene(home, 800, 600);
    }

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
            data.setCenter(createDayView());
        }else{
            //database view
            data.setCenter(createDatabaseView());
        }
        home.setRight(data);
    }

    public BorderPane createDatabaseView(){
        BorderPane recordData = new BorderPane();
        recordData.setPrefSize(600, 600);
        //set header and set recordData.Center
        Label header;
        if(currentView == "Patient"){
            header = new Label("Patient Records");
            header.setPadding(new Insets(5,5,5,5));
            Scheduler.Patient[] patientList = dbAccessor.getPatientRecords().toArray(new Scheduler.Patient[dbAccessor.getPatientRecords().size()]);
            recordData.setCenter(buildRecordsData(patientList, "Patient"));
        }else if(currentView == "Employee"){
            header = new Label("Employee Records");
            Scheduler.Employee[] employeeList = dbAccessor.getEmployeeRecords().toArray(new Scheduler.Employee[dbAccessor.getEmployeeRecords().size()]);
            recordData.setCenter(buildRecordsData(employeeList, "Employee"));
        }else if(currentView == "Doctor"){
            header = new Label("Doctor Records");
            Scheduler.Doctor[] doctorList = dbAccessor.getDoctorRecords().toArray(new Scheduler.Doctor[dbAccessor.getDoctorRecords().size()]);
            recordData.setCenter(buildRecordsData(doctorList, "Doctor"));
        }else{
            header = new Label("Appointment Records: Next 3 Days");
            //Scheduler.Appointment[] apptList = dbAccessor.getAppointments(currentDay).toArray(new Scheduler.Appointment[dbAccessor.getAppointments(currentDay).size()]);
            //apptList.addAll(dbAccessor.getAppointments(currentDay.plusDays(1)));
            //apptList.addAll(dbAccessor.getAppointments(currentDay.plusDays(1)));
            //remove null elements

        }
        header.setPrefSize(600, 50);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-background-color: #d3d3d3");
        header.setAlignment(Pos.CENTER_LEFT);
        recordData.setTop(header);
        //bottom: search by RecordID
        Label search = new Label("Find by ID: ");
        search.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16));
        searchRecordTF = new TextField("Enter ID");
        Button searchRecordBtn = new Button("Search");
        searchRecordBtn.setPrefSize(100, 30);
        searchRecordBtn.setDisable(true);
        searchRecordTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                searchRecordBtn.setDisable(false);
            }
        });
        HBox searchBar = new HBox();
        searchBar.getChildren().addAll(search, searchRecordTF, searchRecordBtn);
        searchBar.setSpacing(4);
        searchBar.setPadding(new Insets(0, 0,0,10));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        recordData.setBottom(searchBar);
        return recordData;
    }

    public ScrollPane buildRecordsData(Object[] list, String recordType){
        String[] fields;
        if(recordType == "Patient"){
            fields = new String[]{"ID", "First Name", "Last Name", "DOB", "SSN", "Phone", "Address", "Email"};
        }else if(recordType == "Appointment"){
            fields = new String[]{"ApptID", "Patient", "Date", "Time", "Doctor", "Reason"};
        }else if(recordType == "Employee"){
            fields = new String[]{"EmployeeID", "Name", "Password", "EmployeeType"};
        }else if(recordType == "Your Appointments") {
            fields = new String[]{"ApptID", "Patient", "Date", "Time", "Doctor", "Reason"};
        }else{
            fields = new String[]{"DoctorID", "Name", "Phone", "Password"};
        }
        GridPane records = new GridPane();
        records.setPrefHeight(550);
        records.setGridLinesVisible(true);
        dataBaseSelection = new ToggleGroup();
        dataBaseSelection.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(newValue == null){
                    addBtn.setDisable(false);
                    editBtn.setDisable(true);
                    delBtn.setDisable(true);
                }else{
                    if(recordType == "Patient"){
                        selectedRecordID = newValue.getUserData().toString();
                        addBtn.setDisable(true);
                        editBtn.setDisable(false);
                        editBtn.setOnAction(editPatientRecordEvent);
                        delBtn.setDisable(false);
                        delBtn.setOnAction(delRecordEvent);

                    }else{
                        addBtn.setDisable(true);
                        delBtn.setDisable(false);
                        delBtn.setOnAction(delRecordEvent);
                    }
                }
            }
        });
        //first row: field headers, get class fields of list[i]
        Label[] val = new Label[fields.length+1];
        Label spaceForRadioButtons = new Label(" ");
        val[0] = spaceForRadioButtons;
        for(int i=0;i<fields.length;i++){
            Label temp = new Label(fields[i]);
            temp.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            temp.setMinWidth(Region.USE_PREF_SIZE);
            temp.setPadding(new Insets(4,4,4,4));
            temp.setAlignment(Pos.CENTER);
            val[i+1] = temp;
        }
        records.addRow(0, val);
        //fill record data
        String[] rowData = new String[fields.length];
        for(int i=0;i<list.length;i++){
            if(recordType == "Patient"){
                selectedPatRec = (Scheduler.Patient) list[i];
                rowData = selectedPatRec.getValues();
            }else if(recordType == "Appointment"){
                selectedAppt = (Scheduler.Appointment) list[i];
                rowData = selectedAppt.getValues();
            }else if(recordType == "Employee"){
                selectedEmpRec = (Scheduler.Employee) list[i];
                rowData = selectedEmpRec.getValues();
            }else if(recordType == "Doctor"){
                selectedDocRec = (Scheduler.Doctor) list[i];
                rowData = selectedDocRec.getValues();
            }else{

            }
            ToggleButton rowSelector = new ToggleButton("Edit");
            rowSelector.setToggleGroup(dataBaseSelection);
            rowSelector.setUserData(rowData[0]);
            for(int j=0;j<rowData.length;j++){
                Label value = new Label(rowData[j]);
                value.setFont(Font.font("Arial", 14));
                value.setMinWidth(Region.USE_PREF_SIZE);
                value.setPadding(new Insets(4,4,4,4));
                value.setAlignment(Pos.CENTER);
                records.add(value, j+1, i+1);
            }

            if(sessionType == "Receptionist") {
                records.add(rowSelector, 0, i + 1);
            }
        }
        ScrollPane recordData = new ScrollPane();
        recordData.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recordData.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        recordData.setContent(records);
        return recordData;
    }

    public BorderPane createDayView(){
        BorderPane dayView = new BorderPane();
        dayView.setPrefSize(600,550);
        //top: searchBar
        HBox searchBar = new HBox();
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setStyle("-fx-padding: 5;" +
                "-fx-background-color: DAE6F3;");
        Label search = new Label("Find Date: ");
        search.setFont(Font.font("Arial",FontWeight.BOLD,FontPosture.ITALIC,16));
        DatePicker searchDate = new DatePicker();
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(event -> {
            //selected day
            if (today.equals(searchDate.getValue()) || searchDate.getValue() == null) {
                prevDayBtn.setDisable(true);
            }else if(searchDate.getValue().isBefore(today)){
                Alert oldDateSelection = new Alert(Alert.AlertType.ERROR);
                oldDateSelection.setHeaderText("Invalid Date Search");
                oldDateSelection.setContentText("Cannot view past schedules");
                oldDateSelection.showAndWait();
                searchDate.setValue(currentDay);
                data.setCenter(createDayView());
            }
            else {
                currentDay = searchDate.getValue();
                date.setText(currentDay.getMonth().toString() + " " + currentDay.getDayOfMonth() + ", " + currentDay.getYear());
                data.setCenter(createDayView());
            }
        });
        searchBar.getChildren().addAll(search, searchDate,searchBtn);
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
        for(int i=7; i<18; i++){
            String timestamp;
            if(i < 12){
                timestamp = i + "am";
            }else if(i==12){
                timestamp = i +"pm";
            }else{
                timestamp = (i-12) + "pm";
            }
            Label t = new Label(timestamp);
            t.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            slot = new HBox(t);
            slot.setPrefSize(20, (apptHeight-50)/11);
            slot.setAlignment(Pos.CENTER);
            times.getChildren().add(slot);
        }
        appointmentData.setLeft(times);
        if(sessionType == "Receptionist") {
            appointmentData.setCenter(fillAppointmentData());
        }else{
            appointmentData.setCenter(fillDoctorAppointmentData());
        }
        return appointmentData;
    }

    public GridPane fillDoctorAppointmentData(){
        //temp
        String doctorID = sessionUserID;
        Scheduler.Appointment appt;
        ArrayList<Scheduler.Appointment> doctorsAppt = dbAccessor.getAppointments(currentDay, Integer.parseInt(doctorID));
        GridPane content = new GridPane();
        Label time = new Label("Time");
        Label apptID = new Label("Appointment ID");
        Label patient = new Label("Patient");
        Label reason = new Label("Reason");
        content.addRow(0, time, apptID, patient, reason);
        for(int i=0; i<11 ;i++){
            appt = doctorsAppt.get(i);
            String t;
            if(i<5){
                t = (i+7) + "am";
            }else if(i==5){
                t = (i+7) + "pm";
            }else{
                t = (i-5) + "pm";
            }
            Label timeslot = new Label(t);
            timeslot.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            if(appt == null){
                apptID = new Label("N/A");
                patient = new Label("N/A");
                reason = new Label("N/A");
            }else{
                apptID = new Label(Integer.toString(appt.getId()));
                patient = new Label(appt.getPatient_Name());
                reason = new Label(appt.getReason());
            }
            content.addRow(i+1, timeslot, apptID, patient, reason);
        }

        return content;
    }

    public ScrollPane fillAppointmentData(){
        dayViewAppointments = new ToggleGroup();
        dayViewAppointments.selectedToggleProperty().addListener(timeSlotSelection);
        //temp
        Scheduler.Doctor[] doctorList = dbAccessor.getDoctorRecords().toArray(new Scheduler.Doctor[dbAccessor.getDoctorRecords().size()]);
        ToggleButton timeslot;
        ScrollPane content = new ScrollPane();
        int apptHeight = 500;
        VBox col;
        HBox data = new HBox();
        dayViewAppointments = new ToggleGroup();
        dayViewAppointments.selectedToggleProperty().addListener(timeSlotSelection);
        for(int i=0; i<doctorList.length;i++){
            //doctor label = doctorID
            String dID = Integer.toString(doctorList[i].getId());
            Label doctor = new Label(dID);
            doctor.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            HBox header = new HBox(doctor);
            header.setPrefSize(50, 50);
            header.setAlignment(Pos.CENTER);
            col = new VBox();
            col.getChildren().add(header);
            //get appointments for each doctor
            Scheduler.Appointment[] doctorsApptByID = dbAccessor.getAppointments(currentDay, Integer.parseInt(dID)).toArray(new Scheduler.Appointment[dbAccessor.getAppointments(currentDay, Integer.parseInt(dID)).size()]);
            for(int j=0; j<11; j++){
                //get time for user data
                String time;
                if(j<5){
                    time = (j+7) + "am";
                }else if(j==5){
                    time = (j+7) + "pm";
                }else{
                    time = (j-5) + "pm";
                }
                //set timeslot toggle buttons for current doctor/column; buttonText = N/A for empty slot, ApptID#: ## for taken slot
                String buttonText;
                if(doctorsApptByID[j] == null){
                //if(dayApptsOfDoctor[j]==null){
                    //empty time slot
                    buttonText = "N/A";
                    timeslot = new ToggleButton("N/A");
                    timeslot.setStyle("-fx-border-style: solid inside;"+
                            "fx-border-width: 1px;"+
                            "fx-border-color: black;");
                    timeslot.setToggleGroup(dayViewAppointments);
                }else{
                    //taken time slot
                    buttonText = doctorsApptByID[j].getId()+"";
                    //buttonText = "ApptID#: " + dayApptsOfDoctor[j].getId();
                    timeslot = new ToggleButton(buttonText);
                    timeslot.setStyle("-fx-border-style: solid inside;"+
                            "fx-border-width: 1px;"+
                            "fx-border-color: black;");
                    timeslot.setToggleGroup(dayViewAppointments);
                }
                //userData: buttonText (availability), date, time, dID)
                timeslot.setUserData(new TimeSlot(buttonText, currentDay.getMonth() + " " +currentDay.getDayOfMonth() + ", " + currentDay.getYear(), time, dID));
                timeslot.setPrefSize(110,(apptHeight-50)/11);
                col.getChildren().add(timeslot);
            }
            data.getChildren().add(col);
        }
        content.setContent(data);
        content.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        content.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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
            appointmentDBView.setPrefWidth(200);
            appointmentDBView.setOnAction(changeViewEvent);
            appointmentDBView.setToggleGroup(viewOptions);
            view.getChildren().addAll(header, dayView, appointmentDBView, patientDBView);
        }else{
            //employee can view: dayview and all appointments
            view.getChildren().addAll(header, dayView, appointmentDBView);
        }
        view.setAlignment(Pos.TOP_CENTER);
        functions.setTop(view);
        //set bottom: logout + exit program + view Profile
        viewProfileBtn = new Button("View Profile");
        viewProfileBtn.setPrefWidth(200);
        viewProfileBtn.setOnAction(viewProfileEvent);
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
        VBox bottom;
        if(sessionType == "Receptionist"){
            Button reminderBtn = new Button("Appt. Reminders");
            reminderBtn.setPrefWidth(200);
            final Color startColor = Color.web("#e08090");
            final Color endColor = Color.web("#80e090");
            final ObjectProperty<Color> color = new SimpleObjectProperty<Color>(startColor);
            final StringBinding flash = Bindings.createStringBinding(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return String.format("-fx-body-color: rgb(%d, %d, %d);",
                            (int) (256*color.get().getRed()),
                            (int) (256*color.get().getGreen()),
                            (int) (256*color.get().getBlue()));
                }
            }, color);
                    reminderBtn.styleProperty().bind(flash);
            final Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(color, startColor)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(color, endColor)));
            reminderBtn.setOnAction(event -> {
                timeline.play();
            });
            //get appointments for the next day: today + 1
            bottom = new VBox(reminderBtn,viewProfileBtn,logoutBtn,exitBtn);
        }else{
            bottom = new VBox(viewProfileBtn,logoutBtn,exitBtn);
        }
        functions.setBottom(bottom);
    }

    public void setDataOptions(){
        VBox data = new VBox();
        data.setSpacing(5);
        Label header = new Label("Data Options");
        header.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16));
        if(sessionType == "Receptionist"){
            data.getChildren().add(header);
            if(currentView == "Day View"){
                addBtn = new Button("Add Appointment");
                addBtn.setDisable(true);
                addBtn.setOnAction(addAppointmentEvent);
                delBtn = new Button("Delete Appointment");
                delBtn.setDisable(true);
                addBtn.setPrefWidth(200);
                delBtn.setPrefWidth(200);
                data.getChildren().addAll(addBtn, delBtn);
            }else if(currentView == "Appointment"){
                delBtn = new Button("Delete Appointment");
                delBtn.setPrefWidth(200);
                delBtn.setDisable(true);
                data.getChildren().add(delBtn);
            }else if(currentView == "Doctor"){
                addBtn = new Button("Add Doctor");
                addBtn.setDisable(false);
                delBtn = new Button("Delete Doctor");
                delBtn.setDisable(true);
                addBtn.setPrefWidth(200);
                delBtn.setPrefWidth(200);
                data.getChildren().addAll(addBtn, delBtn);
            }else if(currentView == "Employee"){
                addBtn = new Button("Add Employee");
                addBtn.setDisable(false);
                delBtn = new Button("Delete Employee");
                delBtn.setDisable(true);
                addBtn.setPrefWidth(200);
                delBtn.setPrefWidth(200);
                data.getChildren().addAll(addBtn, delBtn);
            }else{
                addBtn = new Button("Add Patient");
                addBtn.setDisable(false);
                editBtn = new Button("Edit Patient");
                editBtn.setDisable(true);
                delBtn = new Button("Delete Patient");
                delBtn.setDisable(true);
                addBtn.setPrefWidth(200);
                editBtn.setPrefWidth(200);
                delBtn.setPrefWidth(200);
                data.getChildren().addAll(addBtn, editBtn, delBtn);
            }
            delBtn.setOnAction(event -> {
                if(currentView == "Day View" || currentView == "Appointment"){
                    // deleteAppointmentRecord
                }else if(currentView == "Patient"){
                    // deletePatientRecord
                }else{
                    // deleteEmployee
                }
            });
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
            sessionUserID = "1";
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

    ChangeListener<Toggle> timeSlotSelection = (observable, oldValue, newValue) -> {
        if(newValue == null){
            //unselect a selected time slot
            addBtn.setDisable(true);
            delBtn.setDisable(true);
        }else{
            //buttonData: 0-slotText(n/a) 1-date 2-time 3-doctorID
            String[] buttonData = newValue.getUserData().toString().split("-");
            selected = new TimeSlot(buttonData[0], buttonData[1], buttonData[2], buttonData[3]);
            //empty slot selected
            if(selected.isAvailable){
                addBtn.setDisable(false);
                delBtn.setDisable(true);
            }else{
                addBtn.setDisable(true);
                delBtn.setDisable(false);
            }
        }
    };

    public void disablePrimary(){
        functions.setDisable(true);
        data.setDisable(true);
    }
    public void enablePrimary(){
        functions.setDisable(false);
        data.setDisable(false);
    }

    EventHandler<ActionEvent> delRecordEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            disablePrimary();
            deleteRecordWindow = new Stage();
            Label confirmText = new Label("Delete this Record?");
            Button cancel = new Button("Cancel");
            Button confirm = new Button("Confirm");
            GridPane confirmDeletion = new GridPane();
            confirmDeletion.setPrefSize(250, 300);
            confirmDeletion.addRow(0, confirmText);
            confirmDeletion.addRow(1, cancel, confirm);
            deleteRecordWindow.setScene(new Scene(confirmDeletion));
            deleteRecordWindow.show();
            cancel.setOnAction(event1 -> {
                deleteRecordWindow.close();
                enablePrimary();
            });
            confirm.setOnAction(event1 -> {
                if(currentView == "Appointment"){
                    Scheduler.Appointment temp = new Scheduler.Appointment(Integer.parseInt(selectedRecordID));
                    dbAccessor.removeAppointment(temp);
                }else if(currentView == "Patient"){
                    Scheduler.Patient temp = new Scheduler.Patient(selectedRecordID);
                    dbAccessor.removePatientRecord(temp);
                }else if(currentView == "Doctor"){
                    Scheduler.Doctor temp = new Scheduler.Doctor(selectedRecordID);
                    dbAccessor.removeDoctorRecord(temp);
                }else{
                    Scheduler.Employee temp = new Scheduler.Employee(selectedRecordID);
                    dbAccessor.removeEmployeeRecord(temp);
                }
                Alert confirmDelet = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDelet.setContentText("Record has been deleted");
                confirmDelet.showAndWait();
                deleteRecordWindow.close();
                setData();
                enablePrimary();
            });

        }
    };

    EventHandler<ActionEvent> editPatientRecordEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            disablePrimary();
            ArrayList<Scheduler.Patient> pList = dbAccessor.getPatientRecords();
            int i=0;
            while(i<pList.size()){
                if(pList.get(i).getId() == Integer.parseInt(selectedRecordID)){
                    selectedPatRec = pList.get(i);
                    i = pList.size()-1;
                }else{
                    i++;
                }
            }
            editPatientRecordWindow = new Stage();
            GridPane selRecordData = new GridPane();
            Label header = new Label("Edit Patient: ");
            Label headerVal = new Label(""+selectedPatRec.getId());
            Label fname = new Label("First Name: ");
            Label lname = new Label("Last Name: ");
            Label DOB = new Label("DOB: ");
            Label SSN = new Label("SSN: ");
            Label phone = new Label("Phone: ");
            Label address = new Label("Address: ");
            Label email = new Label("Email: ");
            TextField first = new TextField(selectedPatRec.getFname());
            TextField last = new TextField(selectedPatRec.getLname());
            TextField dob = new TextField(selectedPatRec.getDOB());
            TextField ssn = new TextField(selectedPatRec.getSSN());
            TextField ph = new TextField(selectedPatRec.getPhone());
            TextField add = new TextField(selectedPatRec.getAddress());
            TextField em = new TextField(selectedPatRec.getEmail());
            selRecordData.addRow(0, header, headerVal);
            selRecordData.addRow(1, fname, first);
            selRecordData.addRow(2, lname, last);
            selRecordData.addRow(3, DOB, dob);
            selRecordData.addRow(4, SSN, ssn);
            selRecordData.addRow(5, phone, ph);
            selRecordData.addRow(6, address, add);
            selRecordData.addRow(7, email, em);
            Button cancel = new Button("Cancel");
            cancel.setOnAction(event1 -> {
                editPatientRecordWindow.close();
                enablePrimary();
            });
            Button update = new Button("Update");
            update.setOnAction(event1 -> {
                Scheduler.Patient updatedPatient = new Scheduler.Patient(selectedPatRec.getId(), first.getText(),
                        last.getText(), dob.getText(), ssn.getText(), ph.getText(), add.getText(), em.getText());
                dbAccessor.updatePatientRecord(updatedPatient);
                editPatientRecordWindow.close();
                enablePrimary();
            });
            selRecordData.addRow(8, cancel, update);
            selRecordData.setPrefSize(350,450);
            editPatientRecordWindow.setScene(new Scene(selRecordData));
            editPatientRecordWindow.showAndWait();
            setData();
            enablePrimary();
        }
    };

    EventHandler<ActionEvent> addAppointmentEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            //disable primaryStage components
            disablePrimary();
            Alert invalid = new Alert(Alert.AlertType.ERROR);
            invalid.setHeaderText("Invalid Appointment Date");
            if(currentDay.isBefore(today)){
                invalid.setContentText("Can only schedule a future appointment");
                invalid.showAndWait();
            }else if(currentDay.isEqual(today)){
                //appointments on same day must be made at least 2 hours before appointment time
                int selectedHour = Integer.parseInt(selected.time.substring(0, selected.time.length()-2));
                if(selectedHour >= 1 && selectedHour <= 5){
                    selectedHour += 12;
                }
                int currentHour = LocalTime.now().getHour();
                //display alert if current hour is 2 hours greater than selected hour
                if(currentHour > selectedHour+2){
                    invalid.setContentText("Appointments must be scheduled at least 2 hours prior");
                    invalid.showAndWait();
                }
            }else {
                addAppointmentWindow = new Stage();
                addAppointmentWindow.setTitle("Appointment Scheduler");
                GridPane addApptForm = new GridPane();
                addApptForm.setAlignment(Pos.CENTER);
                addApptForm.setVgap(10);
                addApptForm.setPrefSize(350, 450);
                //Header
                HBox header = new HBox();
                Label heading = new Label("Appointment Information");
                heading.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                header.setPrefHeight(50);
                header.getChildren().add(heading);
                addApptForm.addRow(0, header);
                //temp
                //form fields: patientID, doctorID(not selectable), date, time, reason
                //choose patient
                Label patient = new Label("Patient: ");
                patient.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                TextField searchPatientTF = new TextField("Enter Patient ID");
                searchPatientTF.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if(newValue.trim().isEmpty()){
                            scheduleBtn.setDisable(true);
                        }else{
                            scheduleBtn.setDisable(false);
                        }
                    }
                });
                //create search bar for patient
                HBox pBox = new HBox(patient, searchPatientTF);
                addApptForm.addRow(1, pBox);
                //get data from selected Time Slot
                //display date
                Label date = new Label("Date: ");
                date.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                Label selectedDate = new Label(selected.date);
                selectedDate.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                HBox dBox = new HBox(date, selectedDate);
                dBox.setSpacing(5);
                addApptForm.addRow(2, dBox);
                //display time/duration
                Label time = new Label("Time: ");
                time.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                Label selectedTime = new Label(selected.time);
                selectedTime.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                HBox tBox = new HBox(time, selectedTime);
                tBox.setSpacing(5);
                addApptForm.addRow(3, tBox);
                //display doctorID
                Label doc = new Label("Doctor: ");
                doc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                Label selectedDoctor = new Label(selected.doctorID);
                selectedDoctor.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                HBox docBox = new HBox(doc, selectedDoctor);
                docBox.setSpacing(5);
                addApptForm.addRow(4, docBox);
                Label reason = new Label("Appointment Reason (Optional)");
                reason.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                TextField reasonTF = new TextField();
                reasonTF.setPrefSize(300,100);
                reasonTF.positionCaret(0);
                addApptForm.addRow(5, reason);
                addApptForm.addRow(6, reasonTF);
                scheduleBtn = new Button("Schedule");
                scheduleBtn.setPrefWidth(100);
                scheduleBtn.setDisable(true);
                //get inputted patientID from searchPatientTF
                scheduleBtn.setOnAction(event1 -> {
                    String inputID = searchPatientTF.getText();
                    //check if empty
                    if(!inputID.trim().equals("")){
                        //ID should include only numbers
                        Alert invalidID;
                        if(Pattern.matches("[a-zA-Z]+", inputID) == false){
                            System.out.println("Does not contain character");
                            if(dbAccessor.patientExists(new Scheduler.Patient(inputID))){
                                System.out.println("Exists");
                                int patientID = Integer.parseInt(inputID);
                                //create Appointment
                                Scheduler.Appointment newAppt = new Scheduler.Appointment();
                            }else{
                                invalidID = new Alert(Alert.AlertType.ERROR);
                                invalidID.setHeaderText("Appointment Cannot Be Created");
                                invalidID.setContentText("Selected Patient Not In System");
                                invalidID.showAndWait();
                            }
                        }else{
                            invalidID = new Alert(Alert.AlertType.ERROR);
                            invalidID.setHeaderText("Appointment Cannot Be Created");
                            invalidID.setContentText("Patient ID Invalid");
                            invalidID.showAndWait();
                        }
                    }else{
                        scheduleBtn.setDisable(true);
                    }
                });
                cancelBtn = new Button("Cancel");
                cancelBtn.setPrefWidth(100);
                cancelBtn.setOnAction(event1 -> {
                    addAppointmentWindow.close();
                });
                HBox schedButtons = new HBox(scheduleBtn, cancelBtn);
                schedButtons.setPrefHeight(50);
                schedButtons.setAlignment(Pos.CENTER);
                addApptForm.addRow(7, schedButtons);
                addAppointmentWindow.setScene(new Scene(addApptForm));
                addAppointmentWindow.showAndWait();
                enablePrimary();
            }
        }
    };

    EventHandler<ActionEvent> addNonApptRecord = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            disablePrimary();
            addNonApptWindow = new Stage();
            GridPane form = new GridPane();
            String[] fieldValues;
            Label[] fields;
            TextField[] values;
            Label header;
            if(currentView == "Patient"){
                fieldValues = new String[]{"First Name", "Last Name", "DOB","SSN", "Phone", "Address", "Email"};
                header = new Label("Create New Patient");
            }else if(currentView == "Doctor"){
                fieldValues = new String[]{"Name", "Phone", "Password"};
                header = new Label("Create New Doctor");
            }else{
                fieldValues = new String[]{"Name", "Password", "Employee Type"};
                header = new Label("Create New Employee");
            }
            form.addRow(0, header);
            fields = new Label[fieldValues.length];
            values = new TextField[fieldValues.length];
            for(int i=0; i< fields.length;i++){
                Label f = new Label(fieldValues[i] + ": ");
                values[i] = new TextField();
                form.addRow(i+1,f, values[i]);
            }
            Button cancel = new Button("Cancel");
            cancel.setOnAction(event1 -> {
                addNonApptWindow.close();
                enablePrimary();
            });
            Button create = new Button("Create");
            create.setOnAction(event1 -> {
                if(currentView == "Patient"){
                    dbAccessor.createPatientRecord(new Scheduler.Patient(values[0].getText(), values[1].getText(),values[2].getText(), values[3].getText(), values[4].getText(), values[5].getText(),values[6].getText()));
                }else if(currentView == "Doctor"){
                    dbAccessor.createDoctorRecord(new Scheduler.Doctor(values[0].getText(), values[1].getText(),values[2].getText()));
                }else{
                    if(values[2].getText() == "Receptionist"){
                        dbAccessor.createEmployeeRecord(new Scheduler.Employee(values[0].getText(), values[1].getText(), true));
                    }else{
                        dbAccessor.createEmployeeRecord(new Scheduler.Employee(values[0].getText(), values[1].getText(), false));
                    }
                }
                Alert createdRecordAlert = new Alert(Alert.AlertType.CONFIRMATION);
                createdRecordAlert.setContentText("Record Created");
                createdRecordAlert.showAndWait();
                setData();
                enablePrimary();
            });
            form.addRow(fields.length+2,cancel, create);
            addNonApptWindow.setScene(new Scene(form));
            addNonApptWindow.showAndWait();
            enablePrimary();
        }
    };

    EventHandler<ActionEvent> displaySearchResultEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            Label header = new Label("Profile: ");
            header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            //if Patient, allow editting if Receptionist
            if(currentView == "Patient"){

            }else{

            }
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
            setData();
        }
    };

    //needs getEmployeeInfo(String ID)
    //needs getEmployeeFields()
    EventHandler<ActionEvent> viewProfileEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            disablePrimary();
            userProfileWindow = new Stage();
            userProfileWindow.setTitle("Your Profile");
            userProfileWindow.setResizable(false);
            GridPane profile = new GridPane();
            profile.setAlignment(Pos.CENTER);
            profile.setPadding(new Insets(10,10,10,10));
            profile.setPrefSize(350, 400);
            profile.setHgap(5);
            profile.setVgap(20);
            Label header = new Label("Profile: ");
            header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            header.setPrefHeight(50);
            Label userHeading = new Label("[Employee's Name]");
            userHeading.setPrefHeight(50);
            userHeading.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            profile.addRow(0, header, userHeading);
            Label username = new Label("Employee ID: ");
            username.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            Label currentUN = new Label("[current ID]");
            currentUN.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16));
            userHeading.setFont(Font.font(14));
            profile.addRow(1, username, currentUN);
            Label pw = new Label("Password: ");
            pw.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            currentPW = new TextField("[current password]");
            Button update = new Button("Update");
            currentPW.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    update.setDisable(false);
                }
            });
            profile.addRow(2, pw, currentPW);
            Button cancel = new Button("Exit");
            cancel.setPrefWidth(75);
            cancel.setOnAction(event1 -> {
                userProfileWindow.close();
                userProfileWindow = new Stage();
            });
            update.setPrefWidth(75);
            update.setDisable(true);
            profile.addRow(3, cancel, update);
            //need to fill with real values
            userProfileWindow.setScene(new Scene(profile));
            userProfileWindow.setAlwaysOnTop(true);
            userProfileWindow.showAndWait();
            enablePrimary();
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
