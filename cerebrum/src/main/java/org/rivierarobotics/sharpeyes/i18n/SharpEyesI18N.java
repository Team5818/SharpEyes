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
package org.rivierarobotics.sharpeyes.i18n;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import org.rivierarobotics.sharpeyes.Loader;

import com.google.common.io.Resources;

public final class SharpEyesI18N {

    private static final String ORIGINAL_LOCALE = "en_US";
    private static final Properties lang = new Properties();

    private static void loadLang() {
        loadLang(ORIGINAL_LOCALE);
        Locale localLocale = Locale.getDefault();
        String locale = localLocale.getLanguage() + "_" + localLocale.getCountry();
        if (!ORIGINAL_LOCALE.equals(locale)) {
            loadLang(locale);
        }
    }

    private static void loadLang(String locale) {
        URL langFile;
        try {
            langFile = Loader.getI18N(locale);
        } catch (IllegalArgumentException e) {
            if (locale == ORIGINAL_LOCALE) {
                // this is an error!
                throw e;
            }
            // other locales are allowed to fail quietly
            return;
        }
        try (Reader reader = Resources.asCharSource(langFile, StandardCharsets.UTF_8).openBufferedStream()) {
            lang.load(reader);
        } catch (IOException e) {
            if (locale == ORIGINAL_LOCALE) {
                // this is an error!
                throw new UncheckedIOException(e);
            }
            // other locales are allowed to fail quietly
            return;
        }
    }

    static {
        loadLang();
    }

    public static String t(String property) {
        return lang.getProperty(property, property);
    }

    public static String ft(String property, Object... args) {
        return String.format(t(property), args);
    }

}
