package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Author;

/**
 * Tests of the author DAO.
 *
 * @author Al Amine Ahmed Moussa
 * @author Jonathan Lovelace (integration and polishing)
 */
public class AlAuthorDaoTest {
	/**
	 * The connection to the database.
	 */
	private Connection conn;

	/**
	 * The DAO under test.
	 */
	private AuthorDao authorDao;

	/**
	 * Set up the DB connection and the DAO before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void setUp() throws SQLException, IOException {
		try {
			conn = InMemoryDBFactory.getConnection("library");
		} catch (Exception e) {
			e.printStackTrace();
		}
		authorDao = new AuthorDaoImpl(conn);
	}

	/**
	 * Tear down the database after each test.
	 *
	 * @throws SQLException on database error while closing the connection
	 */
	@AfterEach
	public void tearDown() throws SQLException {
		conn.close();
	}

	/**
	 * Test that creating an author works.
	 *
	 * @throws SQLException on database error
	 */
	@Test
	public void createTest() throws SQLException {
		final String str = "Najoua Bahba";
		assertEquals(str, authorDao.create(str).getName(),
				"created author has expected name");
	}

	/**
	 * Test that retrieving an author works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGet() throws SQLException {
		final Author a = authorDao.create("Ibn Khaldoun");
		assertEquals(a.getName(), authorDao.get(a.getId()).getName(),
				"retrieved author has expected name");
	}
}
