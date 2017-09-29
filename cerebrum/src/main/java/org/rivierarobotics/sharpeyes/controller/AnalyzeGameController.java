package org.rivierarobotics.sharpeyes.controller;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.protos.Game;
import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.sharpeyes.common.FieldDefHelper;
import org.rivierarobotics.sharpeyes.data.DataProvider;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;

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
import javafx.scene.control.TableColumn.SortType;
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
        // re-sort
        dataTable.sort();
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

        addColumn("Weight", fvMaker((m, fv) -> fv.setInteger(computeWeight(m))));

        // sort weight by default
        TableColumn<TeamMatch, ?> weightCol = Iterables.getLast(dataTable.getColumns());
        dataTable.getSortOrder().add(0, weightCol);
        weightCol.setSortType(SortType.DESCENDING);
    }

    private long computeWeight(TeamMatch m) {
        return game.getFieldDefsList().stream()
                .mapToLong(field -> getWeight(field, m.getValuesOrThrow(field.getName())))
                .sum();
    }

    private long getWeight(FieldDefinition field, FieldValue value) {
        if (field.getType() == FieldDefinition.Type.STRING) {
            return 0;
        }
        if (FieldDefHelper.isMultiplierType(field.getType())) {
            return (long) (extractFieldValue(value, x -> (Number) x).doubleValue() * field.getWeights(0));
        }
        String valStr = extractFieldValue(value, String::valueOf);
        int wtIndex = FieldDefHelper.getChoices(field).indexOf(valStr);
        return wtIndex < 0 || wtIndex >= field.getWeightsCount() ? 0 : field.getWeights(wtIndex);
    }

    private Callback<CellDataFeatures<TeamMatch, FieldValue>, ObservableValue<FieldValue>> fvMaker(BiConsumer<TeamMatch, FieldValue.Builder> setter) {
        return p -> {
            TeamMatch m = p.getValue();
            FieldValue.Builder fv = FieldValue.newBuilder();
            setter.accept(m, fv);
            return new SimpleObjectProperty<>(fv.build());
        };
    }

    private static <T> T extractFieldValue(FieldValue val, Function<Object, T> converter) {
        switch (val.getValueCase()) {
            case BOOLE:
                return converter.apply(val.getBoole());
            case FLOATING:
                return converter.apply(val.getFloating());
            case INTEGER:
                return converter.apply(val.getInteger());
            case STR:
                return converter.apply(val.getStr());
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
        col.setComparator(getComparator(name));
        dataTable.getColumns().add(col);
    }

    private Comparator<FieldValue> getComparator(String name) {
        Optional<FieldDefinition> def = findFieldDef(name);
        if (!def.isPresent()) {
            // should never happen -- just use a garbage comparator
            return Comparator.comparing(FieldValue::toString);
        }
        return new Comparator<FieldValue>() {

            @Override
            public int compare(FieldValue o1, FieldValue o2) {
                return ComparisonChain.start()
                        .compare((Comparable<?>) extractFieldValue(o1, Function.identity()), (Comparable<?>) extractFieldValue(o2, Function.identity()))
                        .result();
            }
        };
    }

    private static final FieldDefinition FDEF_WEIGHT = FieldDefinition.newBuilder()
            .setType(FieldDefinition.Type.INTEGER)
            .setNotHasUnit(true)
            .setName("Weight")
            .build();

    private Optional<FieldDefinition> findFieldDef(String name) {
        switch (name) {
            case "Weight":
                return Optional.of(FDEF_WEIGHT);
            default:
        }
        return game.getFieldDefsList().stream().filter(def -> name.equals(def.getName())).findFirst();
    }

    private static boolean isNumber(FieldValue value) {
        switch (value.getValueCase()) {
            case FLOATING:
            case INTEGER:
                return true;
            default:
                return false;
        }
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
                if (isNumber(item)) {
                    setAlignment(Pos.TOP_RIGHT);
                } else {
                    setAlignment(Pos.TOP_LEFT);
                }
                super.setText(extractFieldValue(item, String::valueOf));
                super.setGraphic(null);
            }
        }
    }

}
