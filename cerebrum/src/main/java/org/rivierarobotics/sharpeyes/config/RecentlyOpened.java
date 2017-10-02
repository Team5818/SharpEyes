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
package org.rivierarobotics.sharpeyes.config;

import static com.google.common.base.Preconditions.checkState;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;

public class RecentlyOpened {

    private static final int MAX_RECENTS = 5;

    private static final EventBus BUS = new EventBus("RecentlyOpened");

    public static EventBus getBus() {
        return BUS;
    }

    public static final class Event {

        private static final Event INSTANCE = new Event();

        private Event() {
            checkState(INSTANCE == null);
        }
    }

    public static void pushPath(Path justOpened) {
        ConfigManager.modConfig(config -> {
            List<String> recents = new ArrayList<>(config.getRecentlyOpenedList());
            String openedAbs = justOpened.toAbsolutePath().toString();
            if (Iterables.getFirst(recents, "").equals(openedAbs)) {
                return;
            }
            recents.add(0, openedAbs);
            while (recents.size() > MAX_RECENTS) {
                recents.remove(recents.size() - 1);
            }
            config.clearRecentlyOpened().addAllRecentlyOpened(recents);
            BUS.post(Event.INSTANCE);
        });
    }
}
