package com.st.novatech.springlms.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A factory for a connection to an in-memory database for testing.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryDBFactory {
	private InMemoryDBFactory() {

	}

	/**
	 * Create a connection to an in-memory database. The caller is responsible for
	 * closing the connection when it is done.
	 *
	 * @param database the name of the database
	 * @return the database connection
	 * @throws SQLException on error setting up the connection
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	public static Connection getConnection(final String database)
			throws SQLException, IOException {
		// To log queries to stdout, add ";TRACE_LEVEL_SYSTEM_OUT=3" to initialization string.
		final Connection retval = DriverManager
				.getConnection(String.format("jdbc:h2:mem:%s;mode=mysql", database));
		try (Statement statement = retval.createStatement();
				BufferedReader schemaReader = new BufferedReader(
						new InputStreamReader(InMemoryDBFactory.class
								.getResourceAsStream("/schema.sql")))) {
			String line = schemaReader.readLine();
			while (line != null) {
				statement.executeUpdate(line);
				line = schemaReader.readLine();
			}
		}
		retval.setAutoCommit(false);
		return retval;
	}
}
