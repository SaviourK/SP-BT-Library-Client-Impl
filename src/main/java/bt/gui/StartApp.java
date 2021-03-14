package bt.gui;

import bt.gui.options.Options;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.Objects;

public class StartApp {

    private static final Logger logger = LoggerFactory.getLogger(StartApp.class);

    public static void main(String[] args) {
        configureLogging(Options.LogLevel.TRACE);
        configureSecurity();
        registerLog4jShutdownHook();

       BtAppFrame mainPanel = new BtAppFrame("BitTorrent GUI APP");
    }

    private static void configureLogging(Options.LogLevel logLevel) {
        Level log4jLogLevel;
        switch (Objects.requireNonNull(logLevel)) {
            case NORMAL: {
                log4jLogLevel = Level.INFO;
                break;
            }
            case VERBOSE: {
                log4jLogLevel = Level.DEBUG;
                break;
            }
            case TRACE: {
                log4jLogLevel = Level.TRACE;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown log level: " + logLevel.name());
            }
        }
        Configurator.setLevel("bt", log4jLogLevel);
    }

    private static void configureSecurity() {
        // Starting with JDK 8u152 this is a way to programmatically allow unlimited encryption
        // See http://www.oracle.com/technetwork/java/javase/8u152-relnotes-3850503.html
        String key = "crypto.policy";
        String value = "unlimited";
        try {
            Security.setProperty(key, value);
        } catch (Exception e) {
            logger.error(String.format("Failed to set security property '%s' to '%s'", key, value), e);
        }
    }

    private static void registerLog4jShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (LogManager.getContext() instanceof LoggerContext) {
                    Configurator.shutdown((LoggerContext) LogManager.getContext());
                }
            }
        });
    }
}
