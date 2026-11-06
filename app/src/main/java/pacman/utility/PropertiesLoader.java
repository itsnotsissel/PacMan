package pacman.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    /**
     * Load the properties file with the file name, resolved from the classpath
     * (e.g. app/src/main/resources/properties/... or app/src/test/resources/properties/...)
     * @param propertiesFile: name of the properties file
     * @return Properties object
     */
    public static Properties loadPropertiesFile(String propertiesFile) {
        try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(propertiesFile)) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);
            if ("".equals(prop.getProperty("PacMan.move"))) {
                prop.remove("PacMan.move");
            }

            if ("".equals(prop.getProperty("Pills.location"))) {
                prop.remove("Pills.location");
            }

            if ("".equals(prop.getProperty("Gold.location"))) {
                prop.remove("Gold.location");
            }
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
