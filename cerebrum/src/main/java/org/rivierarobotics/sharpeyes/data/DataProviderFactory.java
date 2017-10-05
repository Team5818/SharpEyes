package org.rivierarobotics.sharpeyes.data;

import javafx.stage.Window;

public interface DataProviderFactory {

    DataProvider create(Window parentWindow);
    
    String getId();
    
}
