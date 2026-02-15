package com.showoff.incidentops.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class IncidentOpsLoggerTest {
    @Test
    void incidentLifecycle_logsInfoMessages() {
        Logger logger = (Logger) LoggerFactory.getLogger(IncidentOpsLogger.class);
        LogCapture capture = LogCapture.attach(logger, Level.DEBUG);
        try {
            IncidentOpsLogger.incidentOpened(" inc-9001 ", " Payments-Api ");
            IncidentOpsLogger.incidentResolved("inc-9001", 42);

            List<ILoggingEvent> events = capture.events();
            assertEquals(2, events.size());
            assertEquals(Level.INFO, events.get(0).getLevel());
            assertTrue(events.get(0).getFormattedMessage().contains("id=INC-9001"));
            assertTrue(events.get(0).getFormattedMessage().contains("service=payments-api"));
            assertEquals(Level.INFO, events.get(1).getLevel());
            assertTrue(events.get(1).getFormattedMessage().contains("durationSeconds=42"));
        } finally {
            capture.close();
        }
    }

    @Test
    void notificationAttempt_logsInfoOrWarn() {
        Logger logger = (Logger) LoggerFactory.getLogger(IncidentOpsLogger.class);
        LogCapture capture = LogCapture.attach(logger, Level.DEBUG);
        try {
            IncidentOpsLogger.notificationAttempt("INC-1", 1, true);
            IncidentOpsLogger.notificationAttempt("INC-1", 2, false);

            List<ILoggingEvent> events = capture.events();
            assertEquals(2, events.size());
            assertEquals(Level.INFO, events.get(0).getLevel());
            assertEquals(Level.WARN, events.get(1).getLevel());
        } finally {
            capture.close();
        }
    }

    @Test
    void samplingRateOrDefault_coversTryCatchAndFallbacks() {
        Logger logger = (Logger) LoggerFactory.getLogger(IncidentOpsLogger.class);
        LogCapture capture = LogCapture.attach(logger, Level.DEBUG);
        try {
            assertEquals(25, IncidentOpsLogger.samplingRateOrDefault("25", 10));
            assertEquals(10, IncidentOpsLogger.samplingRateOrDefault("-1", 10));
            assertEquals(10, IncidentOpsLogger.samplingRateOrDefault(null, 10));
            assertEquals(10, IncidentOpsLogger.samplingRateOrDefault(" ", 10));
            assertEquals(10, IncidentOpsLogger.samplingRateOrDefault("200", 10));
            assertEquals(10, IncidentOpsLogger.samplingRateOrDefault("invalid", 10));

            List<Level> levels = capture.events().stream().map(ILoggingEvent::getLevel).toList();
            assertTrue(levels.contains(Level.DEBUG));
            assertTrue(levels.contains(Level.WARN));
        } finally {
            capture.close();
        }
    }

    @Test
    void loggerMethods_validateInput() {
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.incidentOpened(" ", "payments-api"));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.incidentOpened(null, "payments-api"));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.incidentOpened("INC-1", " "));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.incidentResolved(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.incidentResolved("INC-1", -1));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.notificationAttempt(" ", 1, true));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.notificationAttempt("INC-1", 0, true));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.samplingRateOrDefault("10", -1));
        assertThrows(IllegalArgumentException.class, () -> IncidentOpsLogger.samplingRateOrDefault("10", 101));
    }

    @Test
    void loggingConfiguration_isExternalizedInPropertiesFile() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("logging.properties")) {
            assertTrue(stream != null);
            properties.load(stream);
        }
        assertEquals("showoff-incidentops", properties.getProperty("app.name"));
        assertEquals("INFO", properties.getProperty("root.log.level"));
        assertEquals("DEBUG", properties.getProperty("incident.log.level"));
    }

    private record LogCapture(Logger logger, Level originalLevel, ListAppender<ILoggingEvent> appender) implements AutoCloseable {
        private static LogCapture attach(Logger logger, Level testLevel) {
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            Level originalLevel = logger.getLevel();
            logger.setLevel(testLevel);
            logger.addAppender(appender);
            return new LogCapture(logger, originalLevel, appender);
        }

        private List<ILoggingEvent> events() {
            return appender.list;
        }

        @Override
        public void close() {
            logger.detachAppender(appender);
            logger.setLevel(originalLevel);
            appender.stop();
        }
    }
}
