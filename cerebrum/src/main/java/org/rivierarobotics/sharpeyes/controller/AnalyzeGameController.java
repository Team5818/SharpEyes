/*
 * This file is part of cerebrum, licensed under the MIT License (MIT).
 *
 * Copyright (c) Team5818 <https://github.com/Team5818/SharpEyes>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.rivierarobotics.sharpeyes.controller;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.rivierarobotics.protos.CompactTeamMatch;
import org.rivierarobotics.protos.FieldDefinition;
import org.rivierarobotics.protos.FieldValue;
import org.rivierarobotics.sharpeyes.common.FieldDefHelper;
import org.rivierarobotics.sharpeyes.data.SourcedGame;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class AnalyzeGameController {

    private final SourcedGame game;

    public AnalyzeGameController(SourcedGame game) {
        this.game = game;
    }

    @FXML
    private TableView<CompactTeamMatch> dataTable;

    public void initialize() {
        setupTable();
        loadMatches();
    }

    private void loadMatches() {
        ImmutableList<CompactTeamMatch> matches = game.getGame()
                .getAllMatches()
                .collect(toImmutableList());
        loadData(matches);
    }

    private void loadData(List<CompactTeamMatch> matches) {
        dataTable.getItems().setAll(matches);
        // re-sort
        dataTable.sort();
    }

    private void setupTable() {
        addColumn("Regional", fvMaker((m, fv) -> fv.setStr(m.getRegionalName())));
        addColumn("Team Number", fvMaker((m, fv) -> fv.setInteger(m.getTeamNumber())));
        addColumn("Match Number", fvMaker((m, fv) -> fv.setInteger(m.getMatchNumber())));

        for (int i = 0; i < game.getGame().getCurrentInstance().getFieldDefsCount(); i++) {
            FieldDefinition def = game.getGame().getCurrentInstance().getFieldDefs(i);
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
        TableColumn<CompactTeamMatch, ?> weightCol = Iterables.getLast(dataTable.getColumns());
        dataTable.getSortOrder().add(0, weightCol);
        weightCol.setSortType(SortType.DESCENDING);
    }

    private long computeWeight(CompactTeamMatch m) {
        return game.getGame().getCurrentInstance().getFieldDefsList().stream()
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

    private Callback<CellDataFeatures<CompactTeamMatch, FieldValue>, ObservableValue<FieldValue>> fvMaker(BiConsumer<CompactTeamMatch, FieldValue.Builder> setter) {
        return p -> {
            CompactTeamMatch m = p.getValue();
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

    private void addColumn(String name, Callback<CellDataFeatures<CompactTeamMatch, FieldValue>, ObservableValue<FieldValue>> callback) {
        TableColumn<CompactTeamMatch, FieldValue> col = new TableColumn<>(name);
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
        return game.getGame().getCurrentInstance().getFieldDefsList().stream().filter(def -> name.equals(def.getName())).findFirst();
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

    private static final class AGCTableCell extends TableCell<CompactTeamMatch, FieldValue> {

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
