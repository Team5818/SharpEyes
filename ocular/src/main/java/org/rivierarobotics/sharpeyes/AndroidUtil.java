package org.rivierarobotics.sharpeyes;

import android.content.Intent;
import android.view.View;

import java.util.function.Consumer;

public class AndroidUtil {
    public static void startActivity(View view, Class<?> cls, Consumer<Intent> data) {
        Intent intent = new Intent(view.getContext(), cls);
        data.accept(intent);
        view.getContext().startActivity(intent);
    }
}
