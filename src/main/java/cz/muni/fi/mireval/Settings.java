package cz.muni.fi.mireval;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings class responsible for loading settings from mias.properties Property file.
 * mias.properties file is located in the path specified by the cz.muni.fi.mias.mias.to file or in the working directory.
 *
 * @author Martin Liska
 * @since 14.12.2012
 */
public class Settings {
    
    public static final String MATHML_NAMESPACE_URI = "http://www.w3.org/1998/Math/MathML";

    private static Properties config;

    static {
        config = new Properties();
        try {
            config.load(Settings.class.getResourceAsStream("mireval.properties"));
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.WARNING, "Cannot load properties file");
        }
    }
    
    public static String getQueryingUrl() {
        String result = config.getProperty("URL");
        if (result == null || result.equals("")) {
            System.out.println("Broken properties file.");
            System.exit(2);
        }
        return result;
    }
    
    public static String getTopicsFile() {
        String result = config.getProperty("TOPICS");
        if (result == null || result.equals("")) {
            System.out.println("Broken properties file.");
            System.exit(2);
        }
        return result;
    }

    public static String getQrelsFile() {
        String result = config.getProperty("QRELS");
        if (result == null || result.equals("")) {
            System.out.println("Broken properties file.");
            System.exit(2);
        }
        return result;
    }
    
    public static String getOutputDir() {
        String result = config.getProperty("OUTPUT_DIR");
        if (result == null || result.equals("")) {
            System.out.println("Broken properties file.");
            System.exit(2);
        }
        return result;
    }
}
