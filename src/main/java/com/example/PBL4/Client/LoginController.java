package com.example.PBL4.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;

public class LoginController {
    @FXML
    private Button buttonLogin;
    @FXML
    private TextField portFiled;
    @FXML
    private TextField ipField;
    @FXML
    private Button buttonKetNoi;
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private Label labelAnnounce;
    @FXML
    private TextField usernameSignup;
    @FXML
    private TextField emailSignup;
    @FXML
    private TextField passwordSignup;
    @FXML
    private TextField repasswordSignup;
    @FXML
    private Label labelThongBoaDangKi;
    @FXML
    private TextField emailQuenmatkhau;
    @FXML
    private Label quenmatkhauthongbao;
    private  String ipServer ;
    private  Integer port;
    private  Integer port2 = 8889;
    private  String  myIP;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ArrayList<String> ListfileName;
    public  void processIP() throws UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        String TempmyIP = ip.toString();
        String[] parts = TempmyIP.split("/");
        if (parts.length > 1) {
            myIP = parts[1];
        }
    }
    private Integer countLogin = 0;
    @FXML
    //button đăng nhập
    private void loginButton() throws IOException, ClassNotFoundException, SQLException {
        if(socket != null)
        {

            String userName = username.getText();
            String passWord = password.getText();
            if(countLogin == 0)
                dataOutputStream.writeUTF("LOGIN");
            dataOutputStream.writeUTF(userName);
            dataOutputStream.writeUTF(passWord);
            ArrayList<String> listFile = new ArrayList<>();
            File  directory = new File("DataClient\\");
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    listFile.add(file.getName());
                }
            }
            objectOutputStream.writeObject(listFile);
            String checklogined =dataInputStream.readUTF();
            if (checklogined.equals("logined"))
            {
                JOptionPane.showMessageDialog(null, "Tài khoản của bạn đã đăng nhập ở một nơi khác");
            }
            else {
                String result = dataInputStream.readUTF();
                if (result.equals("Ok")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeCLient.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) buttonLogin.getScene().getWindow();
                    HomeClientController controller2 = loader.getController();
                    controller2.setInfoServer(portFiled.getText(), ipField.getText());
                    controller2.setSocket(socket, dataInputStream, dataOutputStream, objectOutputStream, objectInputStream);
                    controller2.receiveUsername(userName);
                    controller2.receiveAll();

                    stage.setScene(scene);
                    stage.show();


                } else {
                    labelAnnounce.setText("Sai thông tin đăng nhập");

                }
            }
            countLogin++;
        }
        else{
            JOptionPane.showMessageDialog(null, "Kết nối server trước khi đăng nhập");
        }

    }
    @FXML
    private void signUp() throws IOException {
        if(socket != null)
        {
            String userName = usernameSignup.getText();
            String passWord = passwordSignup.getText();
            String email = emailSignup.getText();
            String repassword = repasswordSignup.getText();
            if(!repassword.equals(passWord))
            {
                labelThongBoaDangKi.setText("Mật khẩu chưa trùng khớp");
            } else if (passwordSignup.getText().length() <8) {
                labelThongBoaDangKi.setText("Mật khẩu tối thiểu 8 kí tự");
            } else{
                dataOutputStream.writeUTF("SIGNUP");
                dataOutputStream.writeUTF(userName);
                dataOutputStream.writeUTF(passWord);
                dataOutputStream.writeUTF(email);
                String result = dataInputStream.readUTF();
                if(result.equals("Ok"))
                {
                    labelThongBoaDangKi.setText("Đăng kí thành công");
                }
                else {
                    labelThongBoaDangKi.setText("Tên đăng nhập hoặc email đã đăng kí");
                }
            }
        }
        else{
            JOptionPane.showMessageDialog(null, "Kết nối server trước khi đăng kí");
        }

    }
    @FXML
    private void quenmatkhau() throws IOException {
        if(socket != null)
        {
            String email = emailQuenmatkhau.getText();
            if(email.equals(""))
            {
                quenmatkhauthongbao.setText("Vui lòng nhập thông tin của bạn");
            }
            else{
                dataOutputStream.writeUTF("FORGET");
                dataOutputStream.writeUTF(email);
                String result = dataInputStream.readUTF();
                if(result.equals("Ok"))
                {
                    System.out.println("Ok");
                    String password = dataInputStream.readUTF();

                    quenmatkhauthongbao.setText("Mật khẩu của bạn là "+password);
                }
                else {
                    quenmatkhauthongbao.setText("Thông tin không chính xác");
                }
            }
        }
        else{
            JOptionPane.showMessageDialog(null, "Kết nối server trước khi đăng nhập");
        }
    }
    @FXML
    // button kết nối ở HomeCLient sẽ kết nối với server
    private void connectButton() throws UnknownHostException {
        if(portFiled.getText().equals("") || ipField.getText().equals(""))
        {
            JOptionPane.showMessageDialog(null, "Nhập đầy đủ thông tin kết nối");
        }
        else{
            port = Integer.parseInt(portFiled.getText());
            ipServer = ipField.getText();
            processIP();
            try {
                socket = new Socket(ipServer,port);

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());


                buttonKetNoi.setText("Đã kết nối");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "1. Server chưa được hoạt động \n2. Sai thông tin");
            }
        }
    }
}
