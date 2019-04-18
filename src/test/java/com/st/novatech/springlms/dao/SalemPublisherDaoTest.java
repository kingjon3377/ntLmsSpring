package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Publisher;

/**
 * Tests of the publisher DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemPublisherDaoTest {
	/**
	 * Sample publisher name for tests.
	 */
	private static final String SAMPLE_PUBLISHER_NAME = "The Publisher";
	/**
	 * Sample publisher address for tests.
	 */
	private static final String SAMPLE_PUBLISHER_ADDRESS = "601 New Jersey Ave, Washington, DC 20001";
	/**
	 * Sample publisher phone for tests.
	 */
	private static final String SAMPLE_PUBLISHER_PHONE = "1234567890";

	/**
	 * Database connection.
	 */
	private Connection conn;

	/**
	 * Publisher DAO under test.
	 */
	private PublisherDao publisherDaoImpl;
	/**
	 * Stored publisher from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Publisher testPublisher;
	/**
	 * The database table this DAO deals with.
	 */
	private static final String TABLE = "tbl_publisher";
	/**
	 * The primary key field in the database table.
	 */
	private static final String PRIMARY_KEY = "publisherId";

	/**
	 * Set up database connection, DAO, and test data before each test.
	 *
	 * @throws SQLException on database error
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		publisherDaoImpl = new PublisherDaoImpl(conn);
		testPublisher = publisherDaoImpl.create(SAMPLE_PUBLISHER_NAME,
				SAMPLE_PUBLISHER_ADDRESS, SAMPLE_PUBLISHER_PHONE);
	}

	/**
	 * Delete test data and tear down database connection after each test.
	 * @throws SQLException on database error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		// FIXME?: WARNING maybe something that doesn't call the method we are trying to test
		publisherDaoImpl.delete(testPublisher);
		conn.close();
	}

	private int mySQLSize() throws SQLException {
		final String sql = "SELECT COUNT(" + PRIMARY_KEY + ") AS size FROM " + TABLE + ";";
		final PreparedStatement prepareStatement = conn.prepareStatement(sql);
		try (ResultSet resultSet = prepareStatement.executeQuery()) {
			resultSet.next();
			return resultSet.getInt("size");
		}
	}

	/**
	 * Test that creating a publisher works.
	 * @throws SQLException on database error
	 */
	@Test
	public void createPublisherTest() throws SQLException {
		publisherDaoImpl.delete(testPublisher);

		final int previousSize = mySQLSize();

		testPublisher = publisherDaoImpl.create(SAMPLE_PUBLISHER_NAME,
				SAMPLE_PUBLISHER_ADDRESS, SAMPLE_PUBLISHER_PHONE);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize,
				"creating a publisher increases record count");
		assertEquals(SAMPLE_PUBLISHER_NAME, testPublisher.getName(),
				"new publisher has expected name");
		assertEquals(SAMPLE_PUBLISHER_ADDRESS, testPublisher.getAddress(),
				"new publisher has expected address");
		assertEquals(SAMPLE_PUBLISHER_PHONE, testPublisher.getPhone(),
				"new publisher has expected phone");
	}

	/**
	 * Test that deleting a publisher works.
	 * @throws SQLException on database error
	 */
	@Test
	public void deletePublisherTest() throws SQLException {
		final int previousSize = mySQLSize();

		publisherDaoImpl.delete(testPublisher);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize,
				"Deleting a publisher removes the record");
		assertNull(publisherDaoImpl.get(testPublisher.getId()),
				"Deleted publisher is gone from the database");
	}

	/**
	 * Test that updating a publisher works.
	 * @throws SQLException on database error
	 */
	@DisplayName("Update Correctly")
	@Test
	public void updatePublisherTest() throws SQLException {
		final Publisher newPublisher = new Publisher(testPublisher.getId(),
				"Publisher Person", "123 new address in VA", "9876543210");

		publisherDaoImpl.update(newPublisher);

		final Publisher updatedPublisher = publisherDaoImpl.get(newPublisher.getId());

		assertNotNull(updatedPublisher, "updated publisher is still present");
		assertEquals(newPublisher, updatedPublisher,
				"updated publisher has expected fields");
	}

	/**
	 * Test that retrieval works properly.
	 * @throws SQLException on database error
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGetPublisher() throws SQLException {
		final Publisher foundPublisher = publisherDaoImpl.get(testPublisher.getId());
		assertNotNull(foundPublisher, "record successfully retrieved");
		assertEquals(foundPublisher, testPublisher, "record has expected data");
	}

	/**
	 * Retrieval returns null record on unknown ID.
	 * @throws SQLException on database error
	 */
	@DisplayName("Return null if entry not found")
	@Test
	public void testGetNotFoundPublisher() throws SQLException {
		assertNull(publisherDaoImpl.get(Integer.MAX_VALUE),
				"null returned for unknown ID");
	}

	/**
	 * Test that getting all records works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Publisher> listOfPublishers = publisherDaoImpl.getAll();
		final int publisherSize = mySQLSize();
		assertEquals(listOfPublishers.size(), publisherSize,
				"DAO and SQL agree on number of publishers");
	}
}
