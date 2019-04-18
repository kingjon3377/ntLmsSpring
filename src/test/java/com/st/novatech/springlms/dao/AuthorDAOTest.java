package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.st.novatech.springlms.model.Author;

/**
 * A test case for the author DAO class.
 *
 * @author Jonathan Lovelace
 *
 */
public final class AuthorDAOTest {
	/**
	 * The DAO being tested.
	 */
	private AuthorDao testee;
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
		testee = new AuthorDaoImpl(db);
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
	 * Test that updating authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testUpdate() throws SQLException {
		final Author expected = new Author(1, "changed value");
		final Author created = testee.create("original name");
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
	 * Test that deleting authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDelete() throws SQLException {
		final Author toDelete = new Author(1, "author to delete");
		final List<Author> expected = Arrays.asList(new Author(2, "author one"),
				new Author(3, "author two"), new Author(4, "author three"),
				new Author(5, "author 4"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_author` (`authorName`) VALUES(?)")) {
			statement.setString(1, toDelete.getName());
			statement.executeUpdate();
			for (final Author author : expected) {
				statement.setString(1, author.getName());
				statement.executeUpdate();
			}
		}
		assertEquals(5, testee.getAll().size(), "Has correct size before delete");
		testee.delete(toDelete);
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"Deleted author is gone");
		assertNull(testee.get(1), "Deleted author is gone");
	}

	/**
	 * Test that when the DAO removes an author, all of that author's books are
	 * removed.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDeleteCascade() throws SQLException {
		final Author toDelete = testee.create("author to be deleted");
		final Author toKeep = testee.create("author to keep");
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_book` (`title`, `authId`, `pubId`) VALUES(?, ?, ?)")) {
			statement.setString(1, "title by keep");
			statement.setInt(2, toKeep.getId());
			statement.setNull(3, Types.INTEGER);
			statement.executeUpdate();
			statement.setString(1, "title by delete");
			statement.setInt(2, toDelete.getId());
			statement.setNull(3, Types.INTEGER);
			statement.executeUpdate();
		}
		try (PreparedStatement statement = db
				.prepareStatement("SELECT COUNT(*) AS `count` FROM `tbl_book`")) {
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				assertEquals(2, rs.getInt(1), "Two books before removing author");
			}
			testee.delete(toDelete);
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				assertEquals(1, rs.getInt(1),
						"Removing author removes author's book");
			}
		}

	}

	/**
	 * Test that getting an author through the DAO works properly.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testGet() throws SQLException {
		final List<Author> expected = Arrays.asList(new Author(1, "one author"),
				new Author(2, "two author"), new Author(3, "three author"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_author` (`authorName`) VALUES(?)")) {
			for (final Author author : expected) {
				statement.setString(1, author.getName());
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
	 * Test that getting all authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testGetAll() throws SQLException {
		assertTrue(testee.getAll().isEmpty(),
				"Before adding any authors, getAll() returns empty list");
		final List<Author> expected = Arrays.asList(new Author(1, "first author"),
				new Author(2, "second author"), new Author(3, "third author"));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_author` (`authorName`) VALUES(?)")) {
			for (final Author author : expected) {
				statement.setString(1, author.getName());
				statement.executeUpdate();
			}
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"getAll() returns expected authors");
	}

	/**
	 * Test that creating an author through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testCreate() throws SQLException {
		final Author author = testee.create("test author");
		assertEquals("test author", author.getName(),
				"created author has expected name");
		assertEquals(1, author.getId(), "created author has expected ID");
		final Author another = testee.create("test author");
		assertEquals(2, another.getId(), "second author has expected ID");
		final Author third = testee.create("another author");
		assertEquals(3, third.getId(), "third author has expected ID");
		assertEquals("another author", third.getName(),
				"third author has expected name");
		try (Statement statement = db.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT COUNT(*) AS `count` FROM `tbl_author`")) {
			rs.next();
			assertEquals(3, rs.getInt(1), "table has expected number of rows");
		}
		assertThrows(SQLException.class, () -> testee.create(null));
	}
}
