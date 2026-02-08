package com.ru.mag.db.jdbc.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class InfoDialogController {
    @FXML
    Label infoLabel;

    public void setInfoLabel(String info){
        infoLabel.setText(info);
    }

    public void closeDialog(ActionEvent event){
        ((Stage)(((Node)event.getSource()).getScene().getWindow())).close();
    }

}
