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
