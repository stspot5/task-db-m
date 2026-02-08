package com.ru.mag.db.jdbc.gui;

import com.ru.mag.db.jdbc.util.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;

public class MainController {
    @FXML private TextField textName;
    @FXML
    private void onOpenQueries() {
        try {
            // Увери се, че името на файла съвпада точно (Queries.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Queries.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("По - сложни справки - Рекламна агенция");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Грешка при отваряне на Queries.fxml: " + e.getMessage());
        }
    }

    @FXML
    public void handleOpenTable(ActionEvent event) throws IOException {
        String tableName = textName.getText();
        ResultSet rs = DBUtil.getInstance().getAllFromTable(tableName);

        if (rs == null) return;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TableDialog.fxml"));
        Parent tableParent = fxmlLoader.load();

        TableController tblcontroller = fxmlLoader.getController();
        try {
            tblcontroller.setTableResultset(rs, tableName);
        } catch (Exception e) { e.printStackTrace(); }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Table: " + tableName);
        stage.setScene(new Scene(tableParent));
        stage.show();
    }
}