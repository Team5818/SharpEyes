package org.rivierarobotics.sharpeyes.controller;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import java.util.function.BiConsumer;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.data.DataProvider;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class AnalyzeGameController {

    private final DataProvider dataProvider;
    private final Game game;

    public AnalyzeGameController(DataProvider dataProvider, Game game) {
        this.dataProvider = dataProvider;
        this.game = game;
    }

    @FXML
    private Button getData;

    @FXML
    private Pane tableRecontainer;

    @FXML
    private TableView<TeamMatch> dataTable;

    public void initialize() {
        bindGetData();
        setupTable();
    }

    private void bindGetData() {
        getData.setOnAction(event -> getData());
    }

    private void getData() {
        dataProvider.provideMatches().thenAccept(matches -> {
            Platform.runLater(() -> loadData(matches));
        });
    }

    private void loadData(List<TeamMatch> matches) {
        List<TeamMatch> mistakes = matches.stream()
                .filter(m -> !game.getName().equals(m.getGame()))
                .collect(toImmutableList());
        if (!mistakes.isEmpty()) {
            String name = mistakes.get(0).getGame();
            Alert alert = new Alert(AlertType.ERROR, "Incorrect game '" + name + "', expected '" + game.getName() + "'.");
            alert.setResizable(true);
            alert.showAndWait();
            return;
        }
        dataTable.getItems().setAll(matches);
    }

    private void setupTable() {
        addColumn("Regional", fvMaker((m, fv) -> fv.setStr(m.getRegional())));
        addColumn("Team Number", fvMaker((m, fv) -> fv.setInteger(m.getTeamNumber())));
        addColumn("Match Number", fvMaker((m, fv) -> fv.setInteger(m.getMatchNumber())));

        for (int i = 0; i < game.getFieldDefsCount(); i++) {
            FieldDefinition def = game.getFieldDefs(i);
            String colName = def.getName();
            if (!def.getNotHasUnit()) {
                // ... aka hasUnit :P
                colName += " (" + def.getUnit() + ")";
            }
            addColumn(colName, p -> {
                FieldValue val = p.getValue().getValuesMap().get(def.getName());
                return new SimpleObjectProperty<>(val);
            });
        }
    }

    private Callback<CellDataFeatures<TeamMatch, FieldValue>, ObservableValue<FieldValue>> fvMaker(BiConsumer<TeamMatch, FieldValue.Builder> setter) {
        return p -> {
            TeamMatch m = p.getValue();
            FieldValue.Builder fv = FieldValue.newBuilder();
            setter.accept(m, fv);
            return new SimpleObjectProperty<>(fv.build());
        };
    }

    private static Object extractFieldValue(FieldValue val) {
        switch (val.getValueCase()) {
            case BOOLE:
                return val.getBoole();
            case FLOATING:
                return val.getFloating();
            case INTEGER:
                return val.getInteger();
            case STR:
                return val.getStr();
            default:
                throw new AssertionError("Unhandled case " + val.getValueCase());
        }
    }

    private void addColumn(String name, Callback<CellDataFeatures<TeamMatch, FieldValue>, ObservableValue<FieldValue>> callback) {
        TableColumn<TeamMatch, FieldValue> col = new TableColumn<>(name);
        col.setCellValueFactory(callback);
        col.setCellFactory(tc -> {
            return new AGCTableCell();
        });
        dataTable.getColumns().add(col);
    }

    private static final class AGCTableCell extends TableCell<TeamMatch, FieldValue> {

        @Override
        protected void updateItem(FieldValue item, boolean empty) {
            if (item == getItem())
                return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else {
                switch (item.getValueCase()) {
                    case FLOATING:
                    case INTEGER:
                        setAlignment(Pos.TOP_RIGHT);
                        break;
                    default:
                        setAlignment(Pos.TOP_LEFT);
                }
                super.setText(String.valueOf(extractFieldValue(item)));
                super.setGraphic(null);
            }
        }
    }

}
