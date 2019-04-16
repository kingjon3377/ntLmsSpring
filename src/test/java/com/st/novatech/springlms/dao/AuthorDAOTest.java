package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;

/**
 * A test case for the author DAO class.
 *
 * @author Jonathan Lovelace
 *
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public final class AuthorDAOTest {
	/**
	 * The DAO being tested.
	 */
	@Autowired
	private AuthorDao testee;
	/**
	 * Book DAO used in tests.
	 */
	@Autowired
	private BookDao bookDao;
	/**
	 * The wrapper around the connection to the database.
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;
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
		db = jdbcTemplate.getDataSource().getConnection();
	}

	/**
	 * Test that updating authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testUpdate() throws SQLException {
		final Author expected = new Author(1, "changed value");
		final Author original = new Author(1, "original name");
		final Author created = testee.create("original name");
		assertNotEquals(expected, created,
				"Author is not originally equal to changed value");
		assertEquals(original, testee.findById(1).get(),
				"Original is retrievable from database");
		assertNotEquals(expected, testee.findById(1).get(),
				"Changed value is not yet retrievable");
		testee.save(expected);
		assertEquals(expected, testee.findById(1).get(),
				"After update(), changed value is retrievable");
		assertNotEquals(original, testee.findById(1).get(),
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
		assertEquals(5, testee.findAll().size(), "Has correct size before delete");
		testee.delete(toDelete);
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.findAll()),
				"Deleted author is gone");
		assertFalse(testee.findById(1).isPresent(), "Deleted author is gone");
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
		final Book keptBook = bookDao.create("title by keep", toKeep, null);
		final Book removedBook = bookDao.create("title by delete", toDelete, null);
		assertEquals(new HashSet<>(Arrays.asList(keptBook, removedBook)),
				new HashSet<>(bookDao.findAll()), "Two books before removing author");
		testee.delete(toDelete);
		testee.flush();
		bookDao.flush();
		assertEquals(Collections.singletonList(keptBook), bookDao.findAll(),
				"Removing author removes author's book");
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
		assertEquals(expected.get(0), testee.findById(1).get(),
				"get() returns first author as expected");
		assertEquals(expected.get(2), testee.findById(3).get(),
				"get() returns third author as expected");
		assertEquals(expected.get(1), testee.findById(2).get(),
				"get() returns second author as expected");
		assertFalse(testee.findById(5).isPresent(), "get() returns null on nonexistent author");
	}

	/**
	 * Test that getting all authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testGetAll() throws SQLException {
		assertTrue(testee.findAll().isEmpty(),
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
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.findAll()),
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
		assertEquals(3, testee.findAll().size(),
				"table has expected number of rows");
	}
}
