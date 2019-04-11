package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Publisher;

/**
 * Tests of publisher DAO.
 *
 * @author Al Amine Ahmed Moussa
 * @author Jonathan Lovelace (integration and polishing)
 */
public class AlPublisherDaoTest {
	/**
	 * The DAO under test.
	 */
	private PublisherDao publisherDao;

	/**
	 * The connection to the database.
	 */
	private Connection conn;

	/**
	 * Set up the DB connection and the DAO before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void setUp() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		publisherDao = new PublisherDaoImpl(conn);
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
	 * Test that creating a publisher works.
	 * @throws SQLException on database error
	 */
	@Test
	public void createTest() throws SQLException {
		final String str1 = "Publisher2";
		final String str2 = "AddressTest2";
		final String str3 = "PhoneTest2";

		final Publisher publisher = publisherDao.create(str1, str2, str3);
		assertEquals(str1, publisher.getName(),
				"created publisher has expected name");
		assertEquals(str2, publisher.getAddress(),
				"created publisher has expected address");
		assertEquals(str3, publisher.getPhone(),
				"created publisher has expected phone");
	}

	/**
	 * Test that retrieving a publisher works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGet() throws SQLException {
		final Publisher p = publisherDao.create("Ibn Khaldoun", "ADR45", "PHN45");
		assertEquals(p.getName(), publisherDao.get(p.getId()).getName(),
				"publisher has expected name");
	}

}
