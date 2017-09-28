package org.rivierarobotics.sharpeyes.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.rivierarobotics.protos.Config;
import org.rivierarobotics.sharpeyes.AppFileSystem;

import com.google.protobuf.util.JsonFormat;

public final class ConfigManager {

    private static final Path CONFIG_PATH = AppFileSystem.getCommonConfigDir(true).resolve("config.json");

    private static final ConfigManager INSTANCE = new ConfigManager();

    public static Config load() {
        INSTANCE.loadConfig();
        return get();
    }

    public static Config get() {
        return checkNotNull(INSTANCE.getConfig());
    }

    public static Config loadIfNeeded() {
        Config config = INSTANCE.getConfig();
        if (config == null) {
            return load();
        }
        return config;
    }

    public static void set(Config config) {
        INSTANCE.setConfig(config);
    }

    public static void save() {
        INSTANCE.saveConfig();
    }
    
    public static void modConfig(Consumer<Config.Builder> modifications) {
        Config.Builder active = loadIfNeeded().toBuilder();
        modifications.accept(active);
        set(active.build());
        save();
    }

    private Config config;

    private ConfigManager() {
    }

    private void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            config = Config.newBuilder().build();
            return;
        }
        try (Reader in = Files.newBufferedReader(CONFIG_PATH)) {
            Config.Builder configBuilder = Config.newBuilder();
            JsonFormat.parser().merge(in, configBuilder);
            config = configBuilder.build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Config getConfig() {
        return config;
    }

    private void setConfig(Config config) {
        this.config = config;
    }

    private void saveConfig() {
        try (Writer out = Files.newBufferedWriter(CONFIG_PATH)) {
            JsonFormat.printer().appendTo(config, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
