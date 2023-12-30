package com.example.PBL4.Client;

import com.example.PBL4.Server.Account;
import com.example.PBL4.Server.ClientRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HomeClientController {

    @FXML
    private TextField portTextField;
    @FXML
    private Button buttonConnect;
    @FXML
    private TextField ipTextFileld;
    @FXML
    private Button buttonMain;
    @FXML
    private Button buttonServer;
    @FXML
    private Button buttonRenew;
    @FXML
    private AnchorPane anchorpaneListCLient;
    @FXML
    private AnchorPane listRequestClient;

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
    private String username;
    private ArrayList<String> historyServer = new ArrayList<>();
    private ArrayList<String> historyClient = new ArrayList<>();
    public void receiveUsername(String username)
    {
        this.username = username;
        buttonMain.setText("Xin chào : " +username);
    }
    public void setInfoServer(String port,String ip)
    {
        portTextField.setText(port);
        ipTextFileld.setText(ip);
    }
    public void processIP() throws UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        String TempmyIP = ip.toString();
        String[] parts = TempmyIP.split("/");
        if (parts.length > 1) {
            myIP = parts[1];
        }
    }
    public void receiveAll()
    {
            Thread getFileThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true)
                        {
                            Socket socket1 = new Socket(ipServer, 8889);
                            receiveFiles(socket1);
                            socket1.close();
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            getFileThread.start();
    }
    public void setHistoryServer(String filename)
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String history = formattedDateTime + " : " + filename+"\n";
        this.historyServer.add(history);

    }
    public void setHistoryClient(String filename)
    {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        String history = formattedDateTime + " : " + filename+"\n";
        this.historyServer.add(history);

    }
    @FXML
    private void showHistoryServer()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Danh sách đã nhận từ server");
        String temp="";
        for (String name: historyServer
             ) {
            temp += name;
        }
        alert.setContentText(temp);
        alert.showAndWait();
    }
    @FXML
    private void historyCLient()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Danh sách đã nhận từ client");
        String temp="";
        for (String name: historyClient
        ) {
            temp += name;
        }
        alert.setContentText(temp);
        alert.showAndWait();
    }
    public void receiveFiles(Socket socket) throws IOException, ClassNotFoundException {

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        int numFiles = ois.readInt();

        System.out.println("Nhận " + numFiles + " tệp từ máy chủ.");
        for (int i = 0; i < numFiles; i++) {
            String fileName = ois.readObject().toString();
            setHistoryServer(fileName);
            try (FileOutputStream fos = new FileOutputStream("DataClient\\"+fileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = ois.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
        File fileToDelete = new File("DataClient\\End.txt");
        if (fileToDelete.exists()) {
            try {
                Files.delete(fileToDelete.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private ArrayList<Account> listCLient = new ArrayList<>();
    private int numberCLient=0;
    private int numberRow = 0;
    private int numberRequest = 0;
    public void receiveListAccountWithoutThisSocket()
    {
        Thread thread = new Thread(()->{
            while (true) {

                try {
                    String messageType = objectInputStream.readUTF();
                    if(messageType.equals("ACCOUNT")) {
                        System.out.println("Account");
                        Account listAccountWithoutThisSocket = (Account) objectInputStream.readObject();
                        System.out.println(listAccountWithoutThisSocket.getObjectOutputStream());
                        Boolean key = true;
                        for (Account account : listCLient
                        ) {
                            if (account.getUsername().equals(listAccountWithoutThisSocket.getUsername())) {
                                key = false;
                            }
                        }
                        if (key) {
                            listCLient.add(listAccountWithoutThisSocket);
                            Boolean key1 = true;
                            if (listAccountWithoutThisSocket.getUsername().equals(username)) {
                                key1 = false;
                            }
                            if (key1) {
                                Button newButton = new Button(listAccountWithoutThisSocket.getUsername());
                                newButton.setLayoutX(14 + 140 * numberCLient);
                                newButton.setLayoutY(21.0 + 140 * numberRow);
                                newButton.setPrefHeight(110.0);
                                newButton.setPrefWidth(110.0);
                                newButton.getStyleClass().add("buttonClient");
                                newButton.setOnAction(e -> {
                                    try {
                                        showInfo(listAccountWithoutThisSocket);
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                                numberCLient++;
                                if (numberCLient == 4) {
                                    numberCLient = 0;
                                    numberRow++;
                                }
                                Platform.runLater(() -> {
                                    anchorpaneListCLient.getChildren().add(newButton);
                                });
                            }
                        }
                    }
                    else if (messageType.equals("REQUEST")){
                        System.out.println("Request");
                        ClientRequest clientRequest = (ClientRequest) objectInputStream.readObject();
                        Pane pane = new Pane();
                        pane.setLayoutX(14);
                        pane.setLayoutY(14 + 100*numberRequest);
                        pane.setPrefWidth(200);
                        pane.setPrefHeight(76);
                            TextField textField = new TextField("Có yêu cầu từ "+clientRequest.getAccountRequest());
                            textField.setLayoutX(0);
                            textField.setLayoutY(0);
                            textField.setPrefHeight(50);
                            textField.setPrefWidth(200);

                            Button viewButton = new Button("Xem");
                            viewButton.setLayoutX(0);
                            viewButton.setLayoutY(50);
                            viewButton.setPrefHeight(25);
                            viewButton.setPrefWidth(66);

                            viewButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    String nameFiles = "";
                                    for (String name: clientRequest.getFileChoose()
                                    ) {
                                        nameFiles+= name+"\n";
                                    }
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Danh sách được yêu cầu");
                                    alert.setContentText(nameFiles);
                                    alert.showAndWait();
                                }
                            });

                            Button acceptButton = new Button("Ok");
                            acceptButton.setLayoutX(67);
                            acceptButton.setLayoutY(50);
                            acceptButton.setPrefHeight(25);
                            acceptButton.setPrefWidth(66);
                            acceptButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    try {
                                        RespondRequest(clientRequest);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    Platform.runLater(() -> {
                                        listRequestClient.getChildren().remove(pane);
                                    });
                                }
                            });
                            Button cancelButton = new Button("Hủy");
                            cancelButton.setLayoutX(133);
                            cancelButton.setLayoutY(50);
                            cancelButton.setPrefHeight(25);
                            cancelButton.setPrefWidth(66);
                            cancelButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Platform.runLater(() -> {
                                    listRequestClient.getChildren().remove(pane);

                                });
                            }
                        });

                        pane.getChildren().addAll(viewButton,acceptButton,cancelButton, textField);
                        numberRequest++;
                        Platform.runLater(() -> {
                            listRequestClient.getChildren().add(pane);

                        });
                    } else if (messageType.equals("SENDRESPOND")) {
                        System.out.println("Get");
                        int numFiles = objectInputStream.readInt();

                        System.out.println("Nhận " + numFiles + " tệp từ người dùng.");
                        for (int i = 0; i < numFiles; i++) {
                            String fileName = objectInputStream.readObject().toString();
                            setHistoryClient(fileName);
                            try (FileOutputStream fos = new FileOutputStream("Temp\\"+fileName)) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    } else if (messageType.equals("NotOnline")) {
                        System.out.println("NotOnLine");
                        JOptionPane.showMessageDialog(null,"This client is offline");
                    }
                } catch (IOException | ClassNotFoundException e) {

                   // throw new RuntimeException(e);
                }
            }
        });
        thread.start();

    }
    @FXML
    public void logout() throws IOException {
        this.listCLient.clear();
        this.historyClient.clear();
        this.historyServer.clear();
        this.ListfileName.clear();
        this.numberRow = 0;
        this.numberRequest = 0;
        this.numberCLient = 0;

//        this.dataOutputStream.writeUTF("LOGOUT");
//        this.dataOutputStream.writeUTF(this.username);
        this.dataInputStream.close();
        this.dataOutputStream.close();
        this.objectInputStream.close();
        this.objectOutputStream.close();
        this.socket.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) buttonConnect.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void RespondRequest(ClientRequest clientRequest) throws IOException {
        String usernameRequest = clientRequest.getAccountRequest();
        ArrayList<String> listFileRequest = clientRequest.getFileChoose();
        dataOutputStream.writeUTF("RESPOND");
        dataOutputStream.writeUTF(usernameRequest);
        sendFile(listFileRequest);

    }
    private  void sendFile(ArrayList<String> filesToSend) throws IOException {
        this.objectOutputStream.writeInt(filesToSend.size());
        int len=0;
        while (len < filesToSend.size())
        {
            objectOutputStream.writeObject(filesToSend.get(len));
            objectOutputStream.flush();
            try (FileInputStream fis = new FileInputStream(new File("DataClient\\"+filesToSend.get(len)))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    objectOutputStream.write(buffer, 0, bytesRead);
                }
            }
            len++;
        }
    }
    public void showInfo(Account account) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ListFileAnotherCLient.fxml"));
        Parent root = loader.load();
        ListFileAnotherClient anotherClient = loader.getController();
        anotherClient.getAccount(account);
        anotherClient.getData(socket, dataInputStream, dataOutputStream, objectOutputStream, objectInputStream, myIP,ipServer,username);
        Stage stage = new Stage();
        stage.setTitle(account.getUsername());
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void setSocket(Socket socket,DataInputStream dataInputStream,DataOutputStream dataOutputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        processIP();

        ListfileName = (ArrayList<String>) objectInputStream.readObject();
        receiveListAccountWithoutThisSocket();


    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }
    private boolean isWindowShowing = false;
    @FXML
    private void serverButton(ActionEvent event) {
        if (socket == null) {
            JOptionPane.showMessageDialog(null, "Hãy kết nối tới server");
        } else {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("ListFile.fxml"));
                Parent root = loader.load();
                ListFileController controller2 = loader.getController();
                controller2.receiveData(ListfileName);
                controller2.receiSocket(socket, dataInputStream, dataOutputStream, objectOutputStream, objectInputStream, myIP,ipServer);
                controller2.receiveUsername(username);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));

                //nếu đã mở cửa sổ  trước đó thì không mở nữa
                if (isWindowShowing) {
                    Platform.runLater(() -> {
                        stage.toFront();
                    });
                } else {
                    stage.show();
                    isWindowShowing = true;
                }
                // khi đóng cửa sổ thì tăt trạng thái mở cửa sổ
                stage.setOnHidden(e -> {
                    isWindowShowing = false;
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private  void loadNewCLient()
    {

    }


}
