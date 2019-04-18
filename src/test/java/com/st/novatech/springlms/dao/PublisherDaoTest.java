package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Publisher;

/**
 * A test of the publisher DAO class.
 *
 * @author Jonathan Lovelace
 *
 */
class PublisherDaoTest {
	/**
	 * The DAO being tested.
	 */
	private PublisherDao testee;
	/**
	 * The connection to the database.
	 */
	private Connection db;

	/**
	 * Set up the DB connection and the DAO before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void setUp() throws SQLException, IOException {
		db = InMemoryDBFactory.getConnection("library");
		testee = new PublisherDaoImpl(db);
	}

	/**
	 * Tear down the database after each test.
	 *
	 * @throws SQLException on database error while closing the connection
	 */
	@AfterEach
	public void tearDown() throws SQLException {
		db.close();
	}

	/**
	 * Test that creating a publisher through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public final void testCreate() throws SQLException {
		final Publisher publisher = testee.create("test publisher", "test address",
				"test phone");
		assertEquals("test publisher", publisher.getName(),
				"created publisher has expected name");
		assertEquals(1, publisher.getId(), "created publisher has expected ID");
		assertEquals("test address", publisher.getAddress(),
				"created publisher has expected address");
		assertEquals("test phone", publisher.getPhone(),
				"created publisher has expected phone");
		final Publisher another = testee.create("test publisher", "test address",
				"test phone");
		assertEquals(2, another.getId(), "second publisher has expected ID");
		final Publisher third = testee.create("another publisher", "second address",
				"");
		assertEquals(3, third.getId(), "third publisher has expected ID");
		assertEquals("another publisher", third.getName(),
				"third publisher has expected name");
		try (Statement statement = db.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT COUNT(*) AS `count` FROM `tbl_publisher`")) {
			rs.next();
			assertEquals(3, rs.getInt(1), "table has expected number of rows");
		}
	}

	/**
	 * Test record update.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testUpdate() throws SQLException {
		final Publisher expected = new Publisher(1, "changed value",
				"changed address", "changed phone");
		final Publisher created = testee.create("original name", "original address",
				"address phone");
		assertNotEquals(expected, created,
				"Author is not originally equal to changed value");
		assertEquals(created, testee.get(1),
				"Original is retrievable from database");
		assertNotEquals(expected, testee.get(1),
				"Changed value is not yet retrievable");
		testee.update(expected);
		assertEquals(expected, testee.get(1),
				"After update(), changed value is retrievable");
		assertNotEquals(created, testee.get(1),
				"After update(), original is not retrievable");
	}

	/**
	 * Test record deletion.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testDelete() throws SQLException {
		final Publisher toDelete = new Publisher(1, "publisher to delete", "", "");
		final List<Publisher> expected = Arrays.asList(
				new Publisher(2, "publisher one"), new Publisher(3, "publisher two"),
				new Publisher(4, "publisher three"),
				new Publisher(5, "publisher 4"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_publisher` (`publisherName`, `publisherAddress`, `publisherPhone`) VALUES(?, ?, ?)")) {
			statement.setString(1, toDelete.getName());
			statement.setString(2, toDelete.getAddress());
			statement.setString(3, toDelete.getPhone());
			statement.executeUpdate();
			for (final Publisher publisher : expected) {
				statement.setString(1, publisher.getName());
				statement.setString(2, publisher.getAddress());
				statement.setString(3, publisher.getPhone());
				statement.executeUpdate();
			}
		}
		assertEquals(5, testee.getAll().size(), "Has correct size before delete");
		testee.delete(toDelete);
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"Deleted publisher is gone");
		assertNull(testee.get(1), "Deleted author is gone");
	}

	/**
	 * Test that when the DAO removes a publisher, all of that publisher's books are
	 * removed.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDeleteCascade() throws SQLException {
		final Publisher toDelete = testee.create("author to be deleted", "", "");
		final Publisher toKeep = testee.create("author to keep", "", "");
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_book` (`title`, `authId`, `pubId`) VALUES(?, ?, ?)")) {
			statement.setString(1, "title by keep");
			statement.setNull(2, Types.INTEGER);
			statement.setInt(3, toKeep.getId());
			statement.executeUpdate();
			statement.setString(1, "title by delete");
			statement.setNull(2, Types.INTEGER);
			statement.setInt(3, toDelete.getId());
			statement.executeUpdate();
		}
		try (PreparedStatement statement = db
				.prepareStatement("SELECT COUNT(*) AS `count` FROM `tbl_book`")) {
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				assertEquals(2, rs.getInt(1), "Two books before removing publisher");
			}
			testee.delete(toDelete);
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				assertEquals(1, rs.getInt(1),
						"Removing publisher removes publisher's book");
			}
		}

	}

	/**
	 * Test single-record retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGet() throws SQLException {
		final List<Publisher> expected = Arrays.asList(
				new Publisher(1, "one author", "one address", "one phone"),
				new Publisher(2, "two author", "two address", "two phone"),
				new Publisher(3, "three author", "three address", "three phone"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_publisher` (`publisherName`, `publisherAddress`, `publisherPhone`) VALUES(?, ?, ?)")) {
			for (final Publisher publisher : expected) {
				statement.setString(1, publisher.getName());
				statement.setString(2, publisher.getAddress());
				statement.setString(3, publisher.getPhone());
				statement.executeUpdate();
			}
		}
		assertEquals(expected.get(0), testee.get(1),
				"get() returns first author as expected");
		assertEquals(expected.get(2), testee.get(3),
				"get() returns third author as expected");
		assertEquals(expected.get(1), testee.get(2),
				"get() returns second author as expected");
		assertNull(testee.get(5), "get() returns null on nonexistent author");
	}

	/**
	 * Test full-table retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetAll() throws SQLException {
		assertTrue(testee.getAll().isEmpty(),
				"Before adding any authors, getAll() returns empty list");
		final List<Publisher> expected = Arrays.asList(
				new Publisher(1, "one author", "one address", "one phone"),
				new Publisher(2, "two author", "two address", "two phone"),
				new Publisher(3, "three author", "three address", "three phone"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_publisher` (`publisherName`, `publisherAddress`, `publisherPhone`) VALUES(?, ?, ?)")) {
			for (final Publisher publisher : expected) {
				statement.setString(1, publisher.getName());
				statement.setString(2, publisher.getAddress());
				statement.setString(3, publisher.getPhone());
				statement.executeUpdate();
			}
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"getAll() returns expected authors");
	}
}
