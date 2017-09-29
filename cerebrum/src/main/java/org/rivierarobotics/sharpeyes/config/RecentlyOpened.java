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
