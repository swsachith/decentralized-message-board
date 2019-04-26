package iu.e510.message.board.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class Config implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Config.class);
    private Properties configurations;

    public Config() {
        // if external property file is provided
        String configFilePath = System.getProperty("config.file");
        configurations = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = null;
        try {
            if (configFilePath != null) {
                stream = new FileInputStream(configFilePath);
            } else {
                stream = loader.getResourceAsStream(Constants.CONFIG_FILE_NAME);
            }
            configurations.load(stream);
        } catch (IOException e) {
            logger.error("Error reading the configuration file: " + e.getMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error("Error closing the stream for the configuration file: " + e.getMessage(), e);
                }
            }
        }
    }

    public String getConfig(String key) {
        return configurations.getProperty(key);
    }

    public void setConfig(String key, String value) {
        configurations.setProperty(key, value);
    }
}
