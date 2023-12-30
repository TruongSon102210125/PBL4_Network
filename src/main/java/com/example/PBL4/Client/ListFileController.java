package com.example.PBL4.Client;
import com.example.PBL4.FilesAndIp;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ListFileController implements Initializable {

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
    public ArrayList<String> fileList = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private  String  myIP;
    private String ipServer;
    private String username;
    public void receiveUsername(String username)
    {
        this.username = username;
    }
    public void receiSocket(Socket socket, DataInputStream dataInputStream,DataOutputStream dataOutputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,String myIP,String ipserver)
    {
        this.myIP = myIP;
        this.socket = socket;
        this.ipServer = ipserver;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }
    public void receiveData(ArrayList<String> data) {
        this.fileList = data;
        ObservableList<MyColumn> tableData = FXCollections.observableArrayList();
        for (String files : fileList
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    @FXML
    public void requestAllButton()
    {
        try {
            dataOutputStream.writeUTF("PVS");
            FilesAndIp filesAndIp = new FilesAndIp(fileList,myIP,username);
            objectOutputStream.writeObject(filesAndIp);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    @FXML
    public void requestButton()
    {
         ArrayList<String> fileChoose =new ArrayList<>();
        for (MyColumn rowData : tableView.getItems()) {
            if (rowData.isSelected()) {
                fileChoose.add(rowData.getFileName()+"."+rowData.getFileExtension());
            }
        }
        try {
            dataOutputStream.writeUTF("PVS");
            FilesAndIp filesAndIp = new FilesAndIp(fileChoose,myIP,username);
            objectOutputStream.writeObject(filesAndIp);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    // button trở lại ở ListFIle sẽ trở lại trang homeclient
    public void returnButton(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
 class MyColumn {
    private final StringProperty fileName;
    private final StringProperty fileExtension;
    private final SimpleBooleanProperty selected;

    public MyColumn(String fileName, String fileExtension) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileExtension = new SimpleStringProperty(fileExtension);
        this.selected = new SimpleBooleanProperty(false);
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension.get();
    }

    public StringProperty fileExtensionProperty() {
        return fileExtension;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
}






