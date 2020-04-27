package patient_scheduler;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * This class is used to interact with the SchedulerDB.accdb
 * Tables of the DB: Appointment, Doctor, Patient, Medical Employee, Receptionist
 */
public class Scheduler {
    final static String databaseURL = "jdbc:ucanaccess://src//patient_scheduler//SchedulerDB.accdb";

    /**
     * Opens connection with DB
     * GUI interacts with Scheduler object and associated methods
     */
    public Scheduler(){

        //createPatientRecord("Hendrik", "Nguyen", new Date(04/29/1996), 012345, "714-732-3525", " Cal Poly Pomona", "hendrikn@cpp.edu");
    }

    //for Main.java
    public String[] getPatientFields(){
        return new String[]{"First Name", "Last Name", "DOB", "SSN", "Phone", "Address", "Email"};
    }

    public String[] getEmployeeFields(){
        return new String[]{"First Name", "Last Name", "Phone", "Email"};
    }

    public String[] getAppointmentFields(){
        return new String[]{"Patient", "Date", "Time", "Doctor", "Reason"};
    }

    public Appointment[] getAppointments(LocalDateTime day){

        return new Appointment[]{};
    }

    public Employee[] getEmployees(){

        return new Employee[]{};
    }
    //methods that update SchedulerDB data
    public void createPatientRecord(){

    }

    public void updatePatientRecord(){

    }

    public void deletePatientRecord(){

    }

    public void createPatientContact(){

    }

    public void editPatientContact(){

    }

    public void createDoctorRecord(){

    }

    public void removeDoctorRecord(){

    }

    public void createEmployeeRecord(){

    }

    public void editEmployeeRecord(){

    }

    public void deleteEmployeeRecord(){

    }

    public void createAppointment(){

    }

    public void editAppointment(){

    }

    public void deleteAppointment(){

    }

    //methods for viewing SchedulerDB data

    public void viewPatientInfo(){

    }

    public void viewEmployeeInfo(){

    }

    public void viewAppointmentDetails(){

    }

    public class Appointment{
        private int appointment_ID;
        private int patient_ID;
        private LocalDate appt_Date;
        private LocalTime appt_Time;
        private int doctor_ID;
        private String reason;

        public Appointment() {

        }
        public Appointment(int app_id, int pat_id, LocalDate app_date, LocalTime app_time, int doc_id, String reason) {
            setAppointment_ID(app_id);
            setPatient_ID(pat_id);
            setAppt_Date(app_date);
            setAppt_Time(app_time);
            setDoctor_ID(doc_id);
            this.setReason(reason);
        }
        public int getAppointment_ID() {
            return appointment_ID;
        }
        public void setAppointment_ID(int appointment_ID) {
            this.appointment_ID = appointment_ID;
        }
        public int getPatient_ID() {
            return patient_ID;
        }
        public void setPatient_ID(int patient_ID) {
            this.patient_ID = patient_ID;
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
        public int getDoctor_ID() {
            return doctor_ID;
        }
        public void setDoctor_ID(int doctor_ID) {
            this.doctor_ID = doctor_ID;
        }
        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }

    }


    public static class Patient{
        private String fname, lname, DOB, SSN, phone, address;
        public Patient(String f, String l, String d, String s, String p, String a){
            fname = f;
            lname = l;
            DOB = d;
            SSN = s;
            phone = p;
            address = a;
        }
        private String getFname(){return fname;}
        private String getLname(){return lname;}
        private String getDOB(){return DOB;}
        private String getSSN(){return SSN;}
        private String getPhone(){return phone;}
        private String getAddress(){return address;}
    }

    public static class Employee{
        private String fname, lname, email, phone, pw;
        public Employee(String f, String l,String e, String p, String pw){
            fname = f;
            lname = l;
            email = e;
            phone = p;
            this.pw = pw;
        }
    }

}
