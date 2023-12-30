package com.example.PBL4.Client;

import com.example.PBL4.FilesAndIp;
import com.example.PBL4.Server.Account;
import com.example.PBL4.Server.ClientRequest;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ListFileAnotherClient {
    @FXML
    public TableView<MyColumn> tableView;
    @FXML
    private Button buttonReturn;
    @FXML
    private Button buttonRequest;
    @FXML
    private TableColumn<MyColumn,String> columnName;
    @FXML
    private TableColumn<MyColumn,String> columnExtension;
    @FXML
    private TableColumn<MyColumn, Boolean> columnAction;
    private Account accountClient;
    public ArrayList<String> fileList = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private  String  myIP;
    private String ipServer;
    private String username;
    public void getData(Socket socket, DataInputStream dataInputStream,DataOutputStream dataOutputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,String myIP,String ipserver,String username)
    {
        this.username = username;
        this.myIP = myIP;
        this.socket = socket;
        this.ipServer = ipserver;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }


    public void getAccount(Account account)
    {
        this.accountClient = account;
        receiveData();
    }
    public void receiveData() {

        ObservableList<MyColumn> tableData = FXCollections.observableArrayList();
        for (String files : accountClient.getLisFileName()
        ) {
            String[] file= files.split("\\.");
            String filename = file[0];
            String fileextension = file[1];
            MyColumn column = new MyColumn(filename, fileextension);
            tableData.add(column);

        }
        tableView.setItems(tableData);
        columnName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileName()));
        columnExtension.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileExtension()));

        columnAction.setCellFactory(CheckBoxTableCell.forTableColumn(columnAction));
        columnAction.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isSelected()));

        columnAction.setCellFactory(CheckBoxTableCell.forTableColumn(columnAction));
        columnAction.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());

        columnAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<MyColumn, Boolean> call(TableColumn<MyColumn, Boolean> param) {
                return new TableCell<>() {
                    private final CheckBox checkBox = new CheckBox();
                    {
                        setGraphic(checkBox);
                        setEditable(true);
                        checkBox.setOnAction(event -> {
                            MyColumn rowData = getTableRow().getItem();
                            if (rowData != null) {
                                rowData.setSelected(checkBox.isSelected());
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            checkBox.setSelected(item);
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void returnButton(ActionEvent event)
    {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
    @FXML
    private void requestAll()
    {
        try {
            dataOutputStream.writeUTF("PVP");
            ClientRequest clientRequest = new ClientRequest(username, accountClient.getUsername(), accountClient.getLisFileName());
            objectOutputStream.writeObject(clientRequest);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    @FXML
    private void requestButton()
    {
        ArrayList<String> fileChoose =new ArrayList<>();
        for (MyColumn rowData : tableView.getItems()) {
            if (rowData.isSelected()) {
                fileChoose.add(rowData.getFileName()+"."+rowData.getFileExtension());
            }
        }
        try {
            dataOutputStream.writeUTF("PVP");
            ClientRequest clientRequest = new ClientRequest(username, accountClient.getUsername(), fileChoose);
            objectOutputStream.writeObject(clientRequest);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
