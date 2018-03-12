package org.rivierarobotics.sharpeyes;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import java.util.function.Consumer;

public class AndroidUtil {
    public static void startActivity(View view, Class<?> cls, Consumer<Intent> data) {
        Intent intent = new Intent(view.getContext(), cls);
        data.accept(intent);
        view.getContext().startActivity(intent);
    }

    public static void startActivityForResult(Activity activity, int requestCode, Class<?> cls, Consumer<Intent> data) {
        Intent intent = new Intent(activity, cls);
        data.accept(intent);
        activity.startActivityForResult(intent, requestCode);
    }
}
