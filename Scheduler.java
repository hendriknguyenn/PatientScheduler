package paitent_scheduler;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

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
        try(Connection connection = DriverManager.getConnection(databaseURL)){
            //for testing
            Patient temp = new Patient("hendrik","nguyen","04/29/1996","123123123","714-732-3525", "Cal Poly Pomona");
            createPatientRecord(connection, temp);
            Doctor temp2 = new Doctor("Mike", "911", "password123");
            createDoctorRecord(connection,temp2);
        }catch(SQLException ex){
            System.out.println("Unable to connect to SchedulerDB.accdb");
        }

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

}
