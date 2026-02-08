package com.ru.mag.db.jdbc.gui;

import com.ru.mag.db.jdbc.util.DBUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.sql.*;

public class QueryController {

    @FXML private ComboBox<String> querySelector;
    @FXML private TextField parameterField;
    @FXML private TableView<ObservableList<String>> resultsTable;

    @FXML
    public void initialize() {

        querySelector.setItems(FXCollections.observableArrayList(
                "1. Всички клиенти",
                "2. Клиент по ID (въведи число)",
                "3. Всички договори",
                "4. Договори на клиент по ID (въведи число)",
                "5. Всички плащания",
                "6. Плащания по договор ID (въведи число)",
                "7. Всички фактури",
                "8. Фактури по статус (PAID/UNPAID/CANCELLED)",
                "9. Приходи по клиент (само PAID фактури)",
                "10. Всички реклами + кликове/импресии",
                "11. Активни реклами към днешна дата",
                "12. Топ 5 реклами по кликове",
                "13. Всички служители + отдел + роля",
                "14. Задачи на служител по ID (въведи число)",
                "15. Среден рейтинг по клиент"
        ));

        querySelector.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleExecuteQuery() {

        String selected = querySelector.getValue();
        if (selected == null) return;

        String p = parameterField.getText().trim();

        String sql = null;
        boolean hasParam = false;

        if (selected.startsWith("1.")) {
            sql = "SELECT * FROM klient ORDER BY klient_id";
        }
        else if (selected.startsWith("2.")) {
            sql = "SELECT * FROM klient WHERE klient_id = ?";
            hasParam = true;
        }
        else if (selected.startsWith("3.")) {
            sql = "SELECT d.dogovor_id, d.tip, d.nachalna_data, d.kraina_data, d.opisanie, " +
                    "k.ime || ' ' || k.familia AS klient " +
                    "FROM dogovor d JOIN klient k ON d.klient_id = k.klient_id " +
                    "ORDER BY d.dogovor_id";
        }
        else if (selected.startsWith("4.")) {
            sql = "SELECT d.dogovor_id, d.tip, d.nachalna_data, d.kraina_data, d.opisanie " +
                    "FROM dogovor d WHERE d.klient_id = ? " +
                    "ORDER BY d.dogovor_id";
            hasParam = true;
        }
        else if (selected.startsWith("5.")) {
            sql = "SELECT p.plashtane_id, p.dogovor_id, p.data_na_plashtane, p.opisanie " +
                    "FROM plashtane p ORDER BY p.plashtane_id";
        }
        else if (selected.startsWith("6.")) {
            sql = "SELECT p.plashtane_id, p.data_na_plashtane, p.opisanie " +
                    "FROM plashtane p WHERE p.dogovor_id = ? " +
                    "ORDER BY p.plashtane_id";
            hasParam = true;
        }
        else if (selected.startsWith("7.")) {
            sql = "SELECT f.faktura_id, f.plashtane_id, f.cena, f.data_izdavane, f.status, " +
                    "f.firma_izpulnitel, f.firma_poruchitel " +
                    "FROM faktura f ORDER BY f.faktura_id";
        }
        else if (selected.startsWith("8.")) {
            sql = "SELECT f.faktura_id, f.plashtane_id, f.cena, f.data_izdavane, f.status " +
                    "FROM faktura f WHERE f.status = ? " +
                    "ORDER BY f.faktura_id";
            hasParam = true;
        }
        else if (selected.startsWith("9.")) {
            sql =
                    "SELECT k.klient_id, k.ime || ' ' || k.familia AS klient, " +
                    "NVL(SUM(f.cena),0) AS obshto_prihodi " +
                    "FROM klient k " +
                    "LEFT JOIN dogovor d ON d.klient_id = k.klient_id " +
                    "LEFT JOIN plashtane p ON p.dogovor_id = d.dogovor_id " +
                    "LEFT JOIN faktura f ON f.plashtane_id = p.plashtane_id AND f.status = 'PAID' " +
                    "GROUP BY k.klient_id, k.ime, k.familia " +
                    "ORDER BY obshto_prihodi DESC";
        }
        else if (selected.startsWith("10.")) {
            sql =
                    "SELECT r.reklama_id, r.ime, r.tip, r.data_startirane, r.data_priklyuchvane, " +
                    "a.klikaniya, a.impresii, d.dogovor_id, k.ime || ' ' || k.familia AS klient " +
                    "FROM reklami r " +
                    "JOIN reklama_atributi a ON a.reklama_id = r.reklama_id " +
                    "JOIN dogovor d ON d.dogovor_id = r.dogovor_id " +
                    "JOIN klient k ON k.klient_id = d.klient_id " +
                    "ORDER BY r.reklama_id";
        }
        else if (selected.startsWith("11.")) {
            sql =
                    "SELECT r.reklama_id, r.ime, r.tip, r.data_startirane, r.data_priklyuchvane " +
                    "FROM reklami r " +
                    "WHERE r.data_startirane <= SYSDATE " +
                    "AND (r.data_priklyuchvane IS NULL OR r.data_priklyuchvane >= SYSDATE) " +
                    "ORDER BY r.reklama_id";
        }
        else if (selected.startsWith("12.")) {
            sql =
                    "SELECT * FROM (" +
                    "   SELECT r.reklama_id, r.ime, a.klikaniya, a.impresii " +
                    "   FROM reklami r " +
                    "   JOIN reklama_atributi a ON a.reklama_id = r.reklama_id " +
                    "   ORDER BY a.klikaniya DESC" +
                    ") WHERE ROWNUM <= 5";
        }
        else if (selected.startsWith("13.")) {
            sql =
                    "SELECT s.sluzhitel_id, s.ime, s.familia, s.tip, s.nivo, " +
                    "o.ime AS otdel, r.ime AS rola " +
                    "FROM sluzhitel s " +
                    "JOIN otdel o ON o.otdel_id = s.otdel_id " +
                    "JOIN rola r ON r.rola_id = s.rola_id " +
                    "ORDER BY s.sluzhitel_id";
        }
        else if (selected.startsWith("14.")) {
            sql =
                    "SELECT z.zadacha_id, z.ime, z.opisanie " +
                    "FROM zadacha z WHERE z.sluzhitel_id = ? " +
                    "ORDER BY z.zadacha_id";
            hasParam = true;
        }
        else if (selected.startsWith("15.")) {
            sql =
                    "SELECT k.klient_id, k.ime || ' ' || k.familia AS klient, " +
                    "ROUND(AVG(o.reiting),2) AS sreden_reiting, COUNT(*) AS broi_otzivi " +
                    "FROM klient k " +
                    "JOIN obratna_vruzka o ON o.klient_id = k.klient_id " +
                    "GROUP BY k.klient_id, k.ime, k.familia " +
                    "ORDER BY sreden_reiting DESC";
        }

        if (sql == null) return;

        try {
            if (hasParam) {
                executePrepared(sql, p);
            } else {
                executeSimple(sql);
            }
        } catch (Exception e) {
            showError("Грешка", e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSimple(String sql) throws SQLException {

        resultsTable.getColumns().clear();
        resultsTable.getItems().clear();

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection conn = DBUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            fillTable(rs, data);
        }

        resultsTable.setItems(data);
    }

    private void executePrepared(String sql, String param) throws SQLException {

        resultsTable.getColumns().clear();
        resultsTable.getItems().clear();

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Connection conn = DBUtil.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ако параметърът е число -> setInt, иначе setString
            if (param.matches("\\d+")) {
                ps.setInt(1, Integer.parseInt(param));
            } else {
                ps.setString(1, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                fillTable(rs, data);
            }
        }

        resultsTable.setItems(data);
    }

    private void fillTable(ResultSet rs, ObservableList<ObservableList<String>> data) throws SQLException {

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            final int j = i;
            TableColumn<ObservableList<String>, String> col =
                    new TableColumn<>(metaData.getColumnName(i + 1));
            col.setCellValueFactory(param ->
                    new SimpleStringProperty(param.getValue().get(j)));
            resultsTable.getColumns().add(col);
        }

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getString(i) != null ? rs.getString(i) : "");
            }
            data.add(row);
        }
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("SQL / Приложение");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void onBack(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
