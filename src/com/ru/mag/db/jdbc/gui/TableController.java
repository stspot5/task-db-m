package com.ru.mag.db.jdbc.gui;

import com.ru.mag.db.jdbc.util.DBUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.Node;
import javafx.stage.Stage;

public class TableController {
    @FXML private TableView<ObservableList<String>> tableView1;
    @FXML private Label testLabel;
    private String currentTableName;

    public void setTableResultset(ResultSet resultSet, String labelText) throws SQLException {
        this.currentTableName = labelText;

        testLabel.setText(labelText.toUpperCase() + " List");

        tableView1.getColumns().clear();
        tableView1.getItems().clear();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(metaData.getColumnName(i + 1));

            col.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                return new SimpleStringProperty((row != null && colIndex < row.size()) ? row.get(colIndex) : "");
            });

            tableView1.getColumns().add(col);
        }

        while (resultSet.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                String value = resultSet.getString(i);
                row.add(value != null ? value : "NULL");
            }
            data.add(row);
        }

        tableView1.setItems(data);

        if (data.isEmpty()) {
            System.out.println("ГРЕШКА: Няма данни в ResultSet за таблица " + labelText);
        }
    }

    @FXML
    private void onDeleteRow() {

        ObservableList<String> selectedRow = tableView1.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Моля, изберете ред за изтриване!");
            alert.showAndWait();
            return;
        }

        String idColumnName = tableView1.getColumns().get(0).getText();
        String idValue = selectedRow.get(0);

        String sql = "DELETE FROM " + currentTableName + " WHERE " + idColumnName + " = ?";

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idValue);
            pstmt.executeUpdate();

            tableView1.getItems().remove(selectedRow);
            System.out.println("Изтрито успешно!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onBack(ActionEvent event) {
        // Вземаме прозореца, в който е натиснат бутона
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Затваряме само този прозорец
        stage.close();
    }
    @FXML
    private void onUpdateRow() {
        // 1. Вземаме избрания ред от таблицата
        ObservableList<String> selectedRow = tableView1.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            System.out.println("Моля, изберете ред за промяна!");
            return;
        }

        // 2. Вземаме ID-то (първата колона) и старото име (втората колона)
        String idValue = selectedRow.get(0);
        String columnName = tableView1.getColumns().get(1).getText(); // Втората колона (напр. NAME)

        // 3. Отваряме прозорец за въвеждане на новото име
        TextInputDialog dialog = new TextInputDialog(selectedRow.get(1));
        dialog.setTitle("Обновяване");
        dialog.setHeaderText("Промяна на стойност за ID: " + idValue);
        dialog.setContentText("Въведете ново име:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newValue = result.get();

            // 4. SQL заявка за обновяване
            String sql = "UPDATE " + currentTableName + " SET " + columnName + " = ? WHERE " +
                    tableView1.getColumns().get(0).getText() + " = ?";

            try (Connection conn = DBUtil.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newValue);
                pstmt.setString(2, idValue);

                pstmt.executeUpdate();
                System.out.println("Успешно обновяване!");

                // Опресни таблицата, за да видиш промяната
                onRefresh();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void onRefresh() {
    }

    @FXML
    private void onAddRow() {
        // 1. Вземаме имената на колоните от текущата таблица в интерфейса
        ObservableList<TableColumn<ObservableList<String>, ?>> columns = tableView1.getColumns();
        List<String> newValues = new ArrayList<>();

        // 2. Питаме потребителя за стойност за всяка колона
        for (TableColumn<ObservableList<String>, ?> col : columns) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Добавяне в " + currentTableName);
            dialog.setHeaderText("Колона: " + col.getText());
            dialog.setContentText("Въведете стойност:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                newValues.add(result.get());
            } else {
                return; // Ако потребителят натисне Cancel, спираме процеса
            }
        }
        if (newValues.isEmpty()) {
            System.out.println("Грешка: Няма въведени данни!");
            return; // Спираме изпълнението, за да не гръмне базата
        }

        // 3. Генерираме SQL заявката: INSERT INTO TABLE VALUES (?, ?, ...)
        StringBuilder sql = new StringBuilder("INSERT INTO " + currentTableName + " VALUES (");
        for (int i = 0; i < newValues.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        // 4. Изпълнение към базата
        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < newValues.size(); i++) {
                pstmt.setString(i + 1, newValues.get(i));
            }

            pstmt.executeUpdate();
            // ВАЖНО: Oracle изисква COMMIT, но JDBC обикновено е в Auto-Commit режим.
            // Ако не се появи веднага, добави conn.commit(); тук.

            // 5. Опресняваме таблицата, за да видим новия ред
            ResultSet rs = DBUtil.getInstance().getAllFromTable(currentTableName);
            setTableResultset(rs, currentTableName);

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Грешка при запис");
            alert.setContentText("Невалидни данни: " + e.getMessage());
            alert.showAndWait();
        }

    }

}