import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class con {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final int MAX_PARENT_LEVELS = 6;

    public static Connection getConnection() throws SQLException {
        Properties props = loadProperties();

        String url = setting("DB_URL", props, "url");
        String username = setting("DB_USER", props, "username");
        String password = setting("DB_PASSWORD", props, "password");

        if (url == null || url.isEmpty()) {
            throw new SQLException("Database configuration not found. Looked for "
                    + PROPERTIES_FILE + " in " + System.getProperty("user.dir")
                    + " and up to " + MAX_PARENT_LEVELS + " parent directories, then on the classpath, "
                    + "and neither that file nor the DB_URL environment variable supplied a url. "
                    + "Copy db.properties.example to " + PROPERTIES_FILE + " and set your credentials.");
        }

        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found on the classpath. "
                    + "Expected lib/mysql-connector-j-26.7.0.jar. Cause: " + e.getMessage(), e);
        }

        return DriverManager.getConnection(url, username == null ? "" : username,
                password == null ? "" : password);
    }

    private static String setting(String envKey, Properties props, String propKey) {
        String value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        value = props.getProperty(propKey);
        return value == null ? null : value.trim();
    }

    private static Properties loadProperties() throws SQLException {
        Path found = findPropertiesFile();
        if (found != null) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(found)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                throw new SQLException("Could not read " + found + ": " + e.getMessage(), e);
            }
        }

        Properties fromClasspath = loadFromClasspath();
        if (fromClasspath != null) {
            return fromClasspath;
        }

        return new Properties();
    }

    private static Path findPropertiesFile() {
        Path dir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (int i = 0; dir != null && i <= MAX_PARENT_LEVELS; i++) {
            Path candidate = dir.resolve(PROPERTIES_FILE);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static Properties loadFromClasspath() throws SQLException {
        try (InputStream in = con.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                return null;
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new SQLException("Could not read " + PROPERTIES_FILE
                    + " from the classpath: " + e.getMessage(), e);
        }
    }
}
