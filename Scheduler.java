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
    private Connection c;
    /**
     * Opens the scheduler.
     * GUI interacts with Scheduler object and associated methods
     */
    public Scheduler(){
    	try {
			c = DriverManager.getConnection(databaseURL);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Opens connection to DB and inserts new record into Patient table.
     * The patient argument is instantiated and passed after receiving inputted data fields
     *
     * @param patient provides data for 5 fields of Patient
     * @param c connection to SchedulerDB passed from Scheduler object
     */
    
    //Create Methods
    public void createPatientRecord(Patient patient){
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

    public void createDoctorRecord(Doctor doctor){
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
    
    public void createEmployeeRecord( Employee employee){
        String employeeInfo = "INSERT INTO MedicalEmployee (ME_Name, ME_Password, Is_Receptionist ) VALUE (?, ?, ?)";
        try(PreparedStatement statement = c.prepareStatement(employeeInfo)){
            statement.setString(1, employee.getE_Name());
            statement.setString(2, employee.getE_Password());
            statement.setBoolean(3, employee.isIs_Receptionist());
            statement.executeUpdate();
            System.out.println("New Employee Record Created");
        }catch(SQLException ex){
            System.out.println("Not Able to Create New Employee Record");
        }

    }
    
    public void createAppointment(Appointment appointment) {
    	String query = "INSERT INTO Appointment (Patient_ID, Appt_Date, Time, Doctor_ID, Reason) VALUE (?,?,?,?,?)";
    	try(PreparedStatement statement = c.prepareStatement(query)){
            statement.setInt(1, appointment.getPatient_Id());
            statement.setDate(2, Date.valueOf(appointment.getAppt_Date()));
            statement.setTime(3, Time.valueOf(appointment.getAppt_Time()));
            statement.setInt(4, appointment.getDoctor_Id());
            statement.setString(5, appointment.getReason());
            statement.executeUpdate();
            System.out.println("New Appointment Created");
        }catch(SQLException ex){
            System.out.println("Not Able to Create New Appointment");
        }
    }
    
    
    
    //Update Methods
    public void updatePatientRecord(Patient p) {
    	String query = "UPDATE Patient SET First_Name = '" + p.getFname() + "', Last_Name = '" + p.getLname() + "', Date_of_Birth = '" + p.getDOB() + "', SSN = '" + p.getSSN()  
    			    	 + "',  Phone = '" + p.getPhone() + "', Address = '" + p.getAddress() + "',  Email = '" + p.getEmail() 
    			    	 + "' WHERE Patient_ID = " + p.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Patient Record Successfully Updated");
    	}catch(SQLException ex){
            System.out.println("Not Able to Update Patient Record");
        }
    }
    
    public void updateDoctorRecord(Doctor d) {
    	String query = "UPDATE Doctor SET D_Name = '" + d.getD_Name() + "', Phone = '" + d.getPhone() + "', D_Password = '" + d.getD_Password() + "' WHERE Doctor_ID = " + d.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Doctor Record Successfully Updated");
    	}catch(SQLException ex){
            System.out.println("Not Able to Update Doctor Record");
        }
    }
    
    public void updateEmployeeRecord(Employee e) {
    	String query = "UPDATE MedicalEmployee SET ME_Name = '" + e.getE_Name() + "', E_Password = '" + e.getE_Password() + "', Is_Receptionist = "+ e.isIs_Receptionist() +" WHERE Med_Employee_ID = " + e.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Employee Record Successfully Updated");
    	}catch(SQLException ex){
            System.out.println("Not Able to Update Employee Record");
        }
    }
    
    
    //Remove Methods
    public void removePatientRecord(Patient p) {
    	String query = "DELETE FROM Patient WHERE Patient_ID = " + p.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Patient Record Successfully Deleted");
    	}catch(SQLException ex){
            System.out.println("Not Able to Delete Patient Record");
        }
    }
    
    public void removeDoctorRecord(Doctor d) {
    	String query = "DELETE FROM Doctor WHERE Doctor_ID = " + d.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Doctor Record Successfully Deleted");
    	}catch(SQLException ex){
            System.out.println("Not Able to Delete Doctor Record");
        }
    }
    
    public void removeEmployeeRecord(Employee e) {
    	String query = "DELETE FROM MedicalEmployee WHERE Med_Employee_ID = " + e.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Employee Record Successfully Deleted");
    	}catch(SQLException ex){
            System.out.println("Not Able to Delete Employee Record");
        }
    }

    public void removeAppointment(Appointment a) {
    	String query = "DELETE FROM Appointment WHERE Appointment_ID = " + a.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		statement.executeUpdate();
    		System.out.println("Appointment Successfully Deleted");
    	}catch(SQLException ex){
            System.out.println("Not Able to Delete Appointment");
        }
    }
    
    
    
    //RecordExists Methods
    public boolean patientExists(Patient p) {
    	String query = "SELECT FROM Patient WHERE Patient_ID = " + p.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		ResultSet result = statement.executeQuery();
    		if(result.next()) {
    			return true;
    		}
    	}catch(SQLException ex){
            System.out.println("Connection Error");
        }
    	return false;
    }
    
    public boolean doctorExists(Doctor d) {
    	String query = "SELECT FROM Doctor WHERE Doctor_ID = " + d.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		ResultSet result = statement.executeQuery();
    		if(result.next()) {
    			return true;
    		}
    	}catch(SQLException ex){
            System.out.println("Connection Error");
        }
    	return false;
    }
    
    public boolean employeeExists(Employee e) {
    	String query = "SELECT FROM MedicalEmployee WHERE Med_Employee_ID = " + e.getId();
    	try{
    		PreparedStatement statement = c.prepareStatement(query);
    		ResultSet result = statement.executeQuery();
    		if(result.next()) {
    			return true;
    		}
    	}catch(SQLException ex){
            System.out.println("Connection Error");
        }
    	return false;
    }
    
    
    //Get Methods

    public ArrayList<Appointment> getAppointments(LocalDate date){
        ArrayList<Appointment> result_appts = new ArrayList<Appointment>();
        java.sql.Date currentDayFormatted = java.sql.Date.valueOf(date);
        LocalTime currentTime = LocalTime.of(7, 0);
        LocalTime closeTime = LocalTime.of(18, 0);
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM Appointment WHERE Appt_Date = '" + currentDayFormatted+ "' ORDER BY Time");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
            	while(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime() != currentTime && currentTime != closeTime) {
            		result_appts.add(null);
            		currentTime.plusHours(1);
            	}
                Appointment temp_appt = new Appointment(result.getInt(1));
                temp_appt.setAppt_Time(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime());
                temp_appt.setReason(result.getString(6));

                PreparedStatement patient_st = c.prepareStatement("Select Last_Name, First_Name FROM Patient WHERE Patient_ID = " + (int) result.getObject(2));
                ResultSet patient_result = patient_st.executeQuery();
                if(patient_result.next()) {
                    temp_appt.setPatient_Name(patient_result.getString(1) + ", " + patient_result.getString(2));
                    temp_appt.setPatient_Id(result.getInt(2));
                }

                PreparedStatement doctor_st = c.prepareStatement("Select D_Name FROM Doctor WHERE Doctor_ID = " + (int) result.getObject(5));
                ResultSet doctor_result = doctor_st.executeQuery();
                if(doctor_result.next()) {
                    temp_appt.setDoctor_Name(doctor_result.getString(1));
                    temp_appt.setDoctor_Id(result.getInt(5));
                }

                result_appts.add(temp_appt);


            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(currentTime != closeTime) {
    		result_appts.add(null);
    		currentTime.plusHours(1);
    	}
        return result_appts;
    }

    
    public ArrayList<Appointment> getAppointments(LocalDate date, int doctor_ID){
        ArrayList<Appointment> result_appts = new ArrayList<Appointment>();
        java.sql.Date currentDayFormatted = java.sql.Date.valueOf(date);
        LocalTime currentTime = LocalTime.of(7, 0);
        LocalTime closeTime = LocalTime.of(18, 0);
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM Appointment WHERE Appt_Date = '" + currentDayFormatted+ "' AND Doctor_ID = " + doctor_ID + " ORDER BY Time");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
            	while(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime() != currentTime && currentTime != closeTime) {
            		result_appts.add(null);
            		currentTime.plusHours(1);
            	}
                Appointment temp_appt = new Appointment(result.getInt(1));
                temp_appt.setAppt_Time(((Timestamp)result.getObject(4)).toLocalDateTime().toLocalTime());
                temp_appt.setReason(result.getString(6));

                PreparedStatement patient_st = c.prepareStatement("Select Last_Name, First_Name FROM Patient WHERE Patient_ID = " + (int) result.getObject(2));
                ResultSet patient_result = patient_st.executeQuery();
                if(patient_result.next()) {
                    temp_appt.setPatient_Name(patient_result.getString(1) + ", " + patient_result.getString(2)); 
                    temp_appt.setPatient_Id(result.getInt(2));
                }

                PreparedStatement doctor_st = c.prepareStatement("Select D_Name FROM Doctor WHERE Doctor_ID = " + (int) result.getObject(5));
                ResultSet doctor_result = doctor_st.executeQuery();
                if(doctor_result.next()) {
                    temp_appt.setDoctor_Name(doctor_result.getString(1));
                    temp_appt.setDoctor_Id(result.getInt(5));
                }

                result_appts.add(temp_appt);


            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(currentTime != closeTime) {
    		result_appts.add(null);
    		currentTime.plusHours(1);
    	}
        return result_appts;
    }

    
    public ArrayList<Patient> getPatientRecords(){
        ArrayList<Patient> result_records = new ArrayList<Patient>();
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM Patient");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Patient temp_patient = new Patient(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5),
                        result.getString(6),
                        result.getString(7),
                        result.getString(8)
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
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM MedicalEmployee");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Employee temp_employee = new Employee(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getBoolean(4)
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
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM Doctor");
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

    
    // Searches 
    
    public ArrayList<Patient> searchPatientRecords(String name){
    	ArrayList<Patient> result_records = new ArrayList<Patient>();
    	// search first & last names for string
    	 try {
             PreparedStatement pst = c.prepareStatement("Select * FROM Patient WHERE First_Name LIKE '%" + name + "%' OR Last_Name LIKE '%" + name + "%' ");
             ResultSet result = pst.executeQuery();
             while(result.next()) {
                 Patient temp_patient = new Patient(
                         result.getInt(1),
                         result.getString(2),
                         result.getString(3),
                         result.getString(4),
                         result.getString(5),
                         result.getString(6),
                         result.getString(7),
                         result.getString(8)
                 );
                 result_records.add(temp_patient);
             }
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
    	return result_records;
    }
    
    public ArrayList<Doctor> searchDoctorRecords(String name){
    	ArrayList<Doctor> result_records = new ArrayList<Doctor>();
    	// search name for string
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM Doctor WHERE D_Name LIKE '%" + name + "%'");
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
    
    public ArrayList<Employee> searchEmployeeRecords(String name){
    	ArrayList<Employee> result_records = new ArrayList<Employee>();
    	// search name for string
        try {
            PreparedStatement pst = c.prepareStatement("Select * FROM MedicalEmployee WHERE ME_Name LIKE '%" + name + "%'");
            ResultSet result = pst.executeQuery();
            while(result.next()) {
                Employee temp_employee = new Employee(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getBoolean(4)
                );
                result_records.add(temp_employee);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	return result_records;
    }
    
    
    
    // Classes

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
            setEmail(e);
        }
        public Patient(int i, String f, String l, String d, String s, String p, String a, String e){
            id = i;
            fname = f;
            lname = l;
            DOB = d;
            SSN = s;
            phone = p;
            address = a;
            setEmail(e);
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
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
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
        private boolean is_Receptionist;
        public Employee(String n, String p, boolean r) {
            setE_Name(n);
            setE_Password(p);
            setIs_Receptionist(r);
        }

        public Employee(int i, String n, String p, boolean r) {
            id = i;
            setE_Name(n);
            setE_Password(p);
            setIs_Receptionist(r);
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

		public boolean isIs_Receptionist() {
			return is_Receptionist;
		}

		public void setIs_Receptionist(boolean is_Receptionist) {
			this.is_Receptionist = is_Receptionist;
		}
    }

    public static class Appointment{
        private String patient_Name;
        private int patient_Id;
        private LocalDate appt_Date;
        private LocalTime appt_Time;
        private String doctor_Name;
        private int doctor_Id;
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

		public int getPatient_Id() {
			return patient_Id;
		}

		public void setPatient_Id(int patient_Id) {
			this.patient_Id = patient_Id;
		}

		public int getDoctor_Id() {
			return doctor_Id;
		}

		public void setDoctor_Id(int doctor_Id) {
			this.doctor_Id = doctor_Id;
		}

    }

}