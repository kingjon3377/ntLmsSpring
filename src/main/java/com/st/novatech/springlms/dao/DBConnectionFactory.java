package com.st.novatech.springlms.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A class to encapsulate the method used to connect to the database under
 * normal circumstances.
 *
 * @author Jonathan Lovelace
 *
 */
public final class DBConnectionFactory {
	private DBConnectionFactory() {
		// Do not instantiate.
	}

	/**
	 * Get a connection to the database, taking configuration from the properties
	 * file "db.properties". The caller is responsible for closing the connection
	 * when finished with it.
	 *
	 * @return the database connection
	 * @throws SQLException if something goes wrong
	 * @throws IOException on failure to find or read from the configuration file
	 */
	public static Connection getDatabaseConnection() throws SQLException, IOException {
		return getDatabaseConnection(Paths.get("db.properties"));
	}

	/**
	 * Get a connection to the database, taking configuration from the given
	 * properties file. The caller is responsible for closing the connection when
	 * finished with it.
	 * @param propertiesFile the properties file from which to take configuration
	 * @return the database connection
	 * @throws SQLException if something goes wrong in connecting to the database
	 * @throws IOException on failure to find or read from the configuration file
	 */
	public static Connection getDatabaseConnection(final Path propertiesFile) throws SQLException, IOException {
		final Properties properties = new Properties();
		try (InputStream is = Files.newInputStream(propertiesFile)) {
			properties.load(is);
		}
		final Connection retval = DriverManager.getConnection(properties.getProperty("url"),
				properties.getProperty("username"),
				properties.getProperty("password"));
		retval.setAutoCommit(false);
		return retval;
	}
}
