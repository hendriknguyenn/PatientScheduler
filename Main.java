package patient_scheduler;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends Application{
    private Stage primaryStage;
    private TextField usernameTF, pwTF;
    private Button loginBtn;

    @Override
    public void start(Stage ps) throws Exception {
        primaryStage = ps;
        primaryStage.setTitle("Patient Scheduling System");
        Scheduler test = new Scheduler();
        primaryStage.setScene(createLoginScene());
        primaryStage.show();

    }

    public Scene createLoginScene(){
        Label enterLoginInfoLb = new Label("Enter Login Credentials");
        Label userLb = new Label("Username: ");
        Label pwLb = new Label("Password: ");

        LoginTextFieldListener textFieldListener = new LoginTextFieldListener();
        usernameTF = new TextField();
        pwTF = new TextField();
        usernameTF.textProperty().addListener(textFieldListener);
        pwTF.textProperty().addListener(textFieldListener);
        loginBtn = new Button("Login");

        GridPane gpLogin = new GridPane();
        gpLogin.addRow(0, userLb, usernameTF);
        gpLogin.addRow(1, pwLb, pwTF);
        gpLogin.setVgap(10);
        gpLogin.setHgap(10);
        gpLogin.setAlignment(Pos.CENTER);
        VBox vbLogin = new VBox(20, enterLoginInfoLb, gpLogin, loginBtn);
        vbLogin.setAlignment(Pos.CENTER);
        return new Scene(vbLogin, 475, 375);
    }


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