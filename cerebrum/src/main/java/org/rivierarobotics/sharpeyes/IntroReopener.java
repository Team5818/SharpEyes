package org.rivierarobotics.sharpeyes;

import static org.rivierarobotics.sharpeyes.i18n.SharpEyesI18N.t;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.rivierarobotics.sharpeyes.controller.IntroController;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Handles opening the intro again when all windows are closed (not a strict app
 * exit).
 */
public class IntroReopener {

    /*
     * Basically, all stages that are a part of the application are registered
     * here. We track which ones are visible manually, and when the last window
     * becomes invisible, we re-open the INTRO_STAGE.
     * 
     * Watching list contains all registered stages. We track these for onShow.
     * This list doesn't actually exist, we just attach handlers and assume they
     * won't be GC'd if they will be opened in the future (which should be true)
     * 
     * Visible list contains all visible stages, created using onShow. We track
     * these for onHidden.
     */

    private static boolean appExiting = false;

    public static void setAppExiting(boolean appExiting) {
        IntroReopener.appExiting = appExiting;
    }

    private static final List<Stage> visible = new ArrayList<>();

    public static void registerStage(Stage toWatch) {
        // make sure our handler doesn't cause GC problems
        WeakReference<Stage> gcAble = new WeakReference<>(toWatch);
        toWatch.setOnShown(event -> stageShown(gcAble.get()));
        // pretend the stage is going down, just in case it never shows...
        stageHidden(toWatch);
    }

    private static final Stage INTRO_STAGE = SharpEyes.applyCommonStageConfig(new Stage(), false);

    static {
        Parent intro = Loader.loadFxml("Intro", new IntroController(INTRO_STAGE));
        INTRO_STAGE.setScene(SharpEyes.addStyleSheets(new Scene(intro, 800, 600)));
        INTRO_STAGE.centerOnScreen();
        INTRO_STAGE.setTitle(t("app.title"));
    }

    private static void stageShown(Stage shown) {
        visible.add(shown);
        shown.setOnHidden(event -> stageHidden(shown));
    }

    private static void stageHidden(Stage watched) {
        visible.remove(watched);
        watched.setOnHidden(null);
        if (!visible.isEmpty() || appExiting) {
            return;
        }
        // last stage going down! Resurrect!
        resurrect();
    }

    private static void resurrect() {
        INTRO_STAGE.show();
    }

}
