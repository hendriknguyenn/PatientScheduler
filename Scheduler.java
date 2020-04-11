package patient_scheduler;
import java.sql.*;

/**
 * This class is used to interact with the SchedulerDB.accdb
 * Tables of the DB: Appointment, Doctor, Patient, Medical Employee, Receptionist
 */
public class Scheduler {
    final String databaseURL = "jdbc:ucanaccess://src//patient_scheduler//SchedulerDB.accdb";

    /**
     * Opens the scheduler.
     * GUI interacts with Scheduler object and associated methods
     */
    public Scheduler(){
        //for testing
        Patient temp = new Patient("hendrik","nguyen","04/29/1996","123123123","714-732-3525", "Cal Poly Pomona");
        createPatientRecord(temp);
    }

    /**
     * Opens connection to DB and inserts new record into Patient table.
     * The patient argument is instantiated and passed after receiving inputted data fields
     *
     * @param patient provides data for 5 fields of Patient record
     */
    public void createPatientRecord(Patient patient){
        try(Connection connection = DriverManager.getConnection(databaseURL)){
            String patientInfo = "INSERT INTO Patient (First_Name, Last_Name, Date_of_Birth, SSN, Phone, Address) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(patientInfo);
            preparedStatement.setString(1,  patient.getFname());
            preparedStatement.setString(2,  patient.getLname());
            preparedStatement.setString(3,  patient.getDOB());
            preparedStatement.setString(4,  patient.getSSN());
            preparedStatement.setString(5,  patient.getPhone());
            preparedStatement.setString(6, patient.getAddress());
            preparedStatement.executeUpdate();
            System.out.println("New Record Created");
        } catch (SQLException ex){
            System.out.println("Not Able to Create New Record");
        }
    }

    public void createDoctorRecord(Doctor doctor){

    }

    private static class Patient{
        private String fname, lname, DOB, SSN, phone, address;
        private Patient(String f, String l, String d, String s, String p, String a){
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

    private static class Doctor{
        private String D_Name, Phone, D_Password;
        private Doctor(String n, String p, String pw){
            D_Name = n;
            Phone = p;
            D_Password = pw;
        }
        private String getD_Name(){return D_Name;}
        private String getPhone(){return Phone;}
        private String getD_Password(){return D_Password;}
    }

    public static void main(String[] args) {
        Scheduler test = new Scheduler();

    }
}
