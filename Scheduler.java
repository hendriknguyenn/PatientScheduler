package patient_scheduler;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import patient_scheduler.Scheduler.Appointment;

/**
 * This class is used to interact with the SchedulerDB.accdb
 * Tables of the DB: Appointment, Doctor, Patient, Medical Employee, Receptionist
 */
public class Scheduler {
    final static String databaseURL = "jdbc:ucanaccess://src//patient_scheduler//SchedulerDB.accdb";
    /**
     * Opens the scheduler.
     * GUI interacts with Scheduler object and associated methods
     */
    public Scheduler(){

    }

    /**
     * Opens connection to DB and inserts new record into Patient table.
     * The patient argument is instantiated and passed after receiving inputted data fields
     *
     * @param patient provides data for 5 fields of Patient
     * @param c connection to SchedulerDB passed from Scheduler object
     */
    public void createPatientRecord(Connection c, Patient patient){
        String patientInfo = "INSERT INTO Patient (First_Name, Last_Name, Date_of_Birth, SSN, Phone, Address) VALUES (?, ?, ?, ?, ?, ?)";
        try(PreparedStatement statement = c.prepareStatement(patientInfo)){
            statement.setString(1,  patient.getFname());
            statement.setString(2,  patient.getLname());
            statement.setString(3,  patient.getDOB());
            statement.setString(4,  patient.getSSN());
            statement.setString(5,  patient.getPhone());
            statement.setString(6, patient.getAddress());
            statement.executeUpdate();
            System.out.println("New  Patient Record Created");
        } catch (SQLException ex){
            System.out.println("Not Able to Create New Patient Record");
        }
    }

    public void createDoctorRecord(Connection c, Doctor doctor){
        String doctorInfo = "INSERT INTO Doctor (D_Name, Phone, D_Password) VALUE (?, ?, ?)";
        try(PreparedStatement statement = c.prepareStatement(doctorInfo)){
            statement.setString(1, doctor.getD_Name());
            statement.setString(2, doctor.getPhone());
            statement.setString(3, doctor.getD_Password());
            statement.executeUpdate();
            System.out.println("New Doctor Record Created");
        }catch(SQLException ex){
            System.out.println("Not Able to Create New Doctor Record");
        }

    }

    public Patient getPatient(int patientID){
        Patient temp = new Patient("Hendrik", "Nguyen", "4/29/1996", "123123123", "714-732-3525", "123 Cal Poly Pomona", "hendrikn@cpp.edu");
        return temp;
    }


    public ArrayList<Appointment> getAppointments(LocalDate date){
        ArrayList<Appointment> result_appts = new ArrayList<Appointment>();
        java.sql.Date currentDayFormatted = java.sql.Date.valueOf(date);
        Connection con;
        try {
            con = DriverManager.getConnection(databaseURL);
            PreparedStatement pst = con.prepareStatement("Select * FROM Appointment WHERE Appt_Date = '" + currentDayFormatted+ "' ORDER BY Time");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Appointment temp_appt = new Appointment(result.getInt(1));
                temp_appt.setAppt_Time(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime());
                temp_appt.setReason(result.getString(6));

                PreparedStatement patient_st = con.prepareStatement("Select Last_Name, First_Name FROM Patient WHERE Patient_ID = " + (int) result.getObject(2));
                ResultSet patient_result = patient_st.executeQuery();
                if(patient_result.next()) {
                    temp_appt.setPatient_Name(patient_result.getString(1) + ", " + patient_result.getString(2));
                }

                PreparedStatement doctor_st = con.prepareStatement("Select D_Name FROM Doctor WHERE Doctor_ID = " + (int) result.getObject(5));
                ResultSet doctor_result = doctor_st.executeQuery();
                if(doctor_result.next()) {
                    temp_appt.setDoctor_Name(doctor_result.getString(1));
                }

                result_appts.add(temp_appt);


            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_appts;
    }



    public ArrayList<Appointment> getAppointments(LocalDate date, int doctor_ID){
        ArrayList<Appointment> result_appts = new ArrayList<Appointment>();
        java.sql.Date currentDayFormatted = java.sql.Date.valueOf(date);
        Connection con;
        try {
            con = DriverManager.getConnection(databaseURL);
            PreparedStatement pst = con.prepareStatement("Select * FROM Appointment WHERE Appt_Date = '" + currentDayFormatted+ "' AND Doctor_ID = " + doctor_ID + " ORDER BY Time");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Appointment temp_appt = new Appointment(result.getInt(1));
                temp_appt.setAppt_Time(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime());
                temp_appt.setReason(result.getString(6));

                PreparedStatement patient_st = con.prepareStatement("Select Last_Name, First_Name FROM Patient WHERE Patient_ID = " + (int) result.getObject(2));
                ResultSet patient_result = patient_st.executeQuery();
                if(patient_result.next()) {
                    temp_appt.setPatient_Name(patient_result.getString(1) + ", " + patient_result.getString(2));
                }

                PreparedStatement doctor_st = con.prepareStatement("Select D_Name FROM Doctor WHERE Doctor_ID = " + (int) result.getObject(5));
                ResultSet doctor_result = doctor_st.executeQuery();
                if(doctor_result.next()) {
                    temp_appt.setDoctor_Name(doctor_result.getString(1));
                }

                result_appts.add(temp_appt);


            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_appts;
    }

    public ArrayList<Patient> getPatientRecords(){
        ArrayList<Patient> result_records = new ArrayList<Patient>();
        Connection con;
        try {
            con = DriverManager.getConnection(databaseURL);
            PreparedStatement pst = con.prepareStatement("Select * FROM Patient");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Patient temp_patient = new Patient(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        result.getString(6),
                        result.getString(7)
                );
                result_records.add(temp_patient);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_records;
    }


    public ArrayList<Employee> getEmployeeRecords(){
        ArrayList<Employee> result_records = new ArrayList<Employee>();
        Connection con;
        try {
            con = DriverManager.getConnection(databaseURL);
            PreparedStatement pst = con.prepareStatement("Select * FROM MedicalEmployee");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Employee temp_employee = new Employee(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3)
                );
                result_records.add(temp_employee);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_records;
    }


    public ArrayList<Doctor> getDoctorRecords(){
        ArrayList<Doctor> result_records = new ArrayList<Doctor>();
        Connection con;
        try {
            con = DriverManager.getConnection(databaseURL);
            PreparedStatement pst = con.prepareStatement("Select * FROM Doctor");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Doctor temp_doctor = new Doctor(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4)
                );
                result_records.add(temp_doctor);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_records;
    }


    public static class Patient{
        private int id;
        private String fname, lname, DOB, SSN, phone, address, email;
        public Patient(String f, String l, String d, String s, String p, String a, String e){
            fname = f;
            lname = l;
            DOB = d;
            SSN = s;
            phone = p;
            address = a;
            email = e;
        }
        public Patient(int i, String f, String l, String d, String s, String p, String a){
            id = i;
            fname = f;
            lname = l;
            DOB = d;
            SSN = s;
            phone = p;
            address = a;
        }
        public void setFname(String fname) {
            this.fname = fname;
        }
        public void setLname(String lname) {
            this.lname = lname;
        }
        public void setDOB(String dOB) {
            DOB = dOB;
        }
        public void setSSN(String sSN) {
            SSN = sSN;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
        public void setAddress(String address) {
            this.address = address;
        }
        public String getFname(){return fname;}
        public String getLname(){return lname;}
        public String getDOB(){return DOB;}
        public String getSSN(){return SSN;}
        public String getPhone(){return phone;}
        public String getAddress(){return address;}



        public int getId() {
            return id;
        }
    }

    public static class Doctor{
        private int id;
        private String D_Name, Phone, D_Password;
        public Doctor(String n, String p, String pw){
            D_Name = n;
            Phone = p;
            D_Password = pw;
        }

        public Doctor(int i, String n, String p, String pw){
            id = i;
            D_Name = n;
            Phone = p;
            D_Password = pw;
        }

        public void setD_Name(String d_Name) {
            D_Name = d_Name;
        }
        public void setPhone(String phone) {
            Phone = phone;
        }
        public void setD_Password(String d_Password) {
            D_Password = d_Password;
        }
        public String getD_Name(){return D_Name;}
        public String getPhone(){return Phone;}
        public String getD_Password(){return D_Password;}

        public int getId() {
            return id;
        }
    }

    public static class Employee{
        private int id;
        private String e_Name, e_Password;
        public Employee(String n, String p) {
            setE_Name(n);
            setE_Password(p);
        }

        public Employee(int i, String n, String p) {
            id = i;
            setE_Name(n);
            setE_Password(p);
        }

        public String getE_Password() {
            return e_Password;
        }
        public void setE_Password(String e_Password) {
            this.e_Password = e_Password;
        }
        public String getE_Name() {
            return e_Name;
        }
        public void setE_Name(String e_Name) {
            this.e_Name = e_Name;
        }

        public int getId() {
            return id;
        }
    }

    public static class Appointment{
        private String patient_Name;
        private LocalDate appt_Date;
        private LocalTime appt_Time;
        private String doctor_Name;
        private String reason;
        private int id;

        public Appointment() {

        }

        public Appointment(int i) {
            id = i;
        }

        public LocalDate getAppt_Date() {
            return appt_Date;
        }
        public void setAppt_Date(LocalDate appt_Date) {
            this.appt_Date = appt_Date;
        }
        public LocalTime getAppt_Time() {
            return appt_Time;
        }
        public void setAppt_Time(LocalTime appt_Time) {
            this.appt_Time = appt_Time;
        }
        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }
        public String getPatient_Name() {
            return patient_Name;
        }
        public void setPatient_Name(String patient_Name) {
            this.patient_Name = patient_Name;
        }
        public String getDoctor_Name() {
            return doctor_Name;
        }
        public void setDoctor_Name(String doctor_Name) {
            this.doctor_Name = doctor_Name;
        }

        public int getId() {
            return id;
        }

    }

    //TODO: Methods Needed:
    // getAppointments(Day, DoctorId) return arraylist<appt>
    // getPatientRecords() return arraylist<patient>
    // getEmployeeRecords() return arraylist<employee>
    // getDoctorRecords() return arraylist<doc>
    // getAppointmentRecords() return arraylist<appt>
    /// >>> done, not tested
}