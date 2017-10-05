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
