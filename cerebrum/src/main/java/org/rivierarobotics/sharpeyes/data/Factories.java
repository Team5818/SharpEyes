package org.rivierarobotics.sharpeyes.data;

import org.rivierarobotics.sharpeyes.data.file.FileDataProvider;
import org.rivierarobotics.sharpeyes.data.qrvid.QrVidDataProvider;

import com.google.auto.service.AutoService;

import javafx.stage.Window;

class Factories {

    @AutoService(DataProviderFactory.class)
    public static final class FileDPF implements DataProviderFactory {

        @Override
        public DataProvider create(Window parentWindow) {
            return new FileDataProvider(parentWindow);
        }

        @Override
        public String getId() {
            return "data.provider.file";
        }

    }

    @AutoService(DataProviderFactory.class)
    public static final class QrVidDPF implements DataProviderFactory {

        @Override
        public DataProvider create(Window parentWindow) {
            return new QrVidDataProvider();
        }

        @Override
        public String getId() {
            return "data.provider.qrvid";
        }

    }

}
