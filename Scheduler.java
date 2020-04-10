package patient_scheduler;
import java.sql.*;

public class Scheduler {
    final String databaseURL = "jdbc:ucanaccess://src//patient_scheduler//SchedulerDB.accdb";

    public Scheduler(){
        Patient p = new Patient("a","b","c","d","e");
        //createPatientAccount(p);
    }

    public void createPatientAccount(Patient patient){

        try(Connection connection = DriverManager.getConnection(databaseURL)){
            String patientInfo = "INSERT INTO Patient (First_Name, Last_Name, DOB, SSN, Phone) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(patientInfo);
            preparedStatement.setString(1,  patient.getFname());
            preparedStatement.setString(2,  patient.getLname());
            preparedStatement.setString(3,  patient.getDOB());
            preparedStatement.setString(4,  patient.getSSN());
            preparedStatement.setString(5,  patient.getPhone());
            int row = preparedStatement.executeUpdate();
            System.out.println("New Record Created");
        } catch (SQLException ex){
            System.out.println("Not Able to Create New Record");
        }

    }

    public static void main(String[] args) {
        Scheduler test = new Scheduler();
        Patient patient = new Patient("a","b","c","d","e");
        final String databaseURL = "jdbc:ucanaccess://src//patient_scheduler//SchedulerDB.accdb";
        try(Connection connection = DriverManager.getConnection(databaseURL)){
            String sql = "INSERT INTO Patient (First Name, Last Name, DOB, SSN, Phone) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            System.out.println("hi");
            preparedStatement.setString(1,  "patient.getFname()");
            preparedStatement.setString(2,  "patient.getLname()");
            preparedStatement.setString(3,  "patient.getDOB()");
            preparedStatement.setString(4,  "patient.getSSN()");
            preparedStatement.setString(5,  "patient.getPhone()");

            int row = preparedStatement.executeUpdate();


            System.out.println("New Record Created");
        } catch (SQLException ex){
            System.out.println("Not Able to Create New Record");
        }

    }



    private static class Patient{
        private String fname, lname, DOB, SSN, phone;
        private Patient(String f, String l, String d, String s, String p){
            fname = f;
            lname = l;
            DOB = d;
            SSN = s;
            phone = p;

        }
        private String getFname(){return fname;}
        private String getLname(){return lname;}
        private String getDOB(){return DOB;}
        private String getSSN(){return SSN;}
        private String getPhone(){return phone;}

    }
    /*
        try(Connection connection = DriverManager.getConnection(databaseURL)){
            System.out.println("works");
        } catch (SQLException ex){
            System.out.println("fails");
        }
    */



}
