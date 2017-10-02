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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Strings;

/**
 * Manages consistent and compatible paths for configuration and other
 * directories.
 */
public class AppFileSystem {

    private static final Path HOME = Paths.get(System.getProperty("user.home"));
    private static final Path BASE_RANDOM_DIR = getBaseRandomDir();
    private static final Path BASE_CONFIG_DIR = getBaseConfigDir();
    static {
        ensureDirectory(BASE_RANDOM_DIR, false);
        ensureDirectory(BASE_CONFIG_DIR, false);
    }

    private static Path ensureDirectory(Path path, boolean create) {
        if (!Files.isDirectory(path)) {
            Exception ex = null;
            try {
                if (create) {
                    Files.createDirectories(path);
                } else if (Files.exists(path)) {
                    // not a directory, and exists -- ERROR
                    ex = new IllegalStateException("File exists");
                }
            } catch (IOException e) {
                ex = e;
            }
            if (ex != null) {
                throw new IllegalStateException(path.toAbsolutePath() + " is not a directory", ex);
            }
        }
        return path;
    }

    private static Path getBaseConfigDir() {
        String xdgConfig = System.getenv("XDG_CONFIG_HOME");
        if (Strings.isNullOrEmpty(xdgConfig)) {
            return HOME.resolve(".config");
        }
        return Paths.get(xdgConfig);
    }

    private static Path getBaseRandomDir() {
        String ranDir = System.getenv("SE_RANDOM_DIR");
        if (Strings.isNullOrEmpty(ranDir)) {
            return Paths.get("./random/");
        }
        return Paths.get(ranDir);
    }

    public static Path getConfigDir(String appName, boolean create) {
        return ensureDirectory(BASE_CONFIG_DIR.resolve(appName), create);
    }

    public static Path getCommonConfigDir(boolean create) {
        return getConfigDir("SharpEyes", create);
    }

    public static Path getRandomDir(String name, boolean create) {
        return ensureDirectory(BASE_RANDOM_DIR.resolve(name), create);
    }

}
