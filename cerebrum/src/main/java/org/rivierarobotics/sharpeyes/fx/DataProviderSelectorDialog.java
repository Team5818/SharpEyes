package org.rivierarobotics.sharpeyes.fx;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.rivierarobotics.sharpeyes.Loader;
import org.rivierarobotics.sharpeyes.data.DataProvider;
import org.rivierarobotics.sharpeyes.data.DataProviderFactory;
import org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N;

import com.google.common.collect.ImmutableList;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;

public final class DataProviderSelectorDialog {

    private static final List<DataProviderFactory> PROVIDERS = ImmutableList.copyOf(ServiceLoader.load(DataProviderFactory.class));

    public static DataProviderSelectorDialog create() {
        return new DataProviderSelectorDialog();
    }

    private final Dialog<DataProviderFactory> dialog;

    private DataProviderSelectorDialog() {
        this.dialog = new Dialog<>();
        dialog.setTitle("Select Data Source");
        fillDialog();
    }

    private void fillDialog() {
        ListView<DataProviderFactory> list = createProviderList(() -> {
            // double click handler
            Node lookupButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            lookupButton.fireEvent(new ActionEvent(lookupButton, lookupButton));
        });
        dialog.getDialogPane().setHeaderText("Select a data source:");
        dialog.getDialogPane().setContent(list);
        dialog.setResultConverter(btn -> {
            return btn == ButtonType.OK ? list.getSelectionModel().getSelectedItem() : null;
        });
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

    private ListView<DataProviderFactory> createProviderList(Runnable cellDoubleClick) {
        ListView<DataProviderFactory> view = new ListView<>();
        view.setCellFactory(v -> {
            return new ListCell<DataProviderFactory>() {

                {
                    setOnMouseClicked(event -> {
                        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                            cellDoubleClick.run();
                        }
                    });
                }

                @Override
                public void updateItem(DataProviderFactory item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(SharpEyesI18N.t(item.getId() + ".name"));
                        setGraphic(new ImageView(Loader.loadImage("icons/" + item.getId())));
                    }
                }
            };
        });
        view.getItems().setAll(PROVIDERS);
        return view;
    }

    public Dialog<DataProviderFactory> getDialog() {
        return dialog;
    }

    public Optional<DataProvider> show(Window parentWindow) {
        return dialog.showAndWait().map(f -> f.create(parentWindow));
    }

}
