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

import com.st.novatech.springlms.model.Author;

/**
 * Tests of the author DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemAuthorDaoTest {
	/**
	 * Sample author name for tests.
	 */
	private static final String SAMPLE_AUTHOR_NAME = "Robert Jr.";

	/**
	 * The connection to the database.
	 */
	private Connection conn;

	/**
	 * The DAO under test.
	 */
	private AuthorDao authorDaoImpl;
	/**
	 * Stored author from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Author testAuthor;
	/**
	 * The table this DAO accesses.
	 */
	private static final String TABLE = "tbl_author";
	/**
	 * The primary key in the table this DAO accesses.
	 */
	private static final String KEY_FIELD = "authorId";

	/**
	 * Set up the DB connection, the DAO, and test data before running each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		authorDaoImpl = new AuthorDaoImpl(conn);
		testAuthor = authorDaoImpl.create(SAMPLE_AUTHOR_NAME);
	}

	/**
	 * Remove the test author from the database and tear the database down after each test.
	 * @throws SQLException on DB error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		// FIXME?: WARNING maybe something that doesn't call the method we are trying to test
		authorDaoImpl.delete(testAuthor);
		conn.close();
	}

	private int mySQLSize() throws SQLException {
		final String sql = "SELECT COUNT(" + KEY_FIELD + ") AS size FROM " + TABLE
				+ ";";
		final PreparedStatement prepareStatement = conn.prepareStatement(sql);
		try (ResultSet resultSet = prepareStatement.executeQuery()) {
			resultSet.next();
			return resultSet.getInt("size");
		}
	}

	/**
	 * Test that author creation works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void createTest() throws SQLException {
		authorDaoImpl.delete(testAuthor);

		final int previousSize = mySQLSize();

		testAuthor = authorDaoImpl.create(SAMPLE_AUTHOR_NAME);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize, "Creation adds a row");
		assertEquals(SAMPLE_AUTHOR_NAME, testAuthor.getName(), "new author has expected name");
	}

	/**
	 * Test that deletion works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void deleteTest() throws SQLException {
		final int previousSize = mySQLSize();

		authorDaoImpl.delete(testAuthor);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize, "Deletion removes a row");
		assertNull(authorDaoImpl.get(testAuthor.getId()), "row is gone after deletion");
	}

	/**
	 * Test that updating works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void updateTest() throws SQLException {
		final Author newAuthor = new Author(testAuthor.getId(), "Author Person");

		authorDaoImpl.update(newAuthor);

		final Author updatedAuthor = authorDaoImpl.get(newAuthor.getId());

		assertNotNull(updatedAuthor, "row is still present after update");
		assertEquals(newAuthor, updatedAuthor, "update was propagated to row");
	}

	/**
	 * Test that retrieval works.
	 * @throws SQLException on DB error.
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGet() throws SQLException {
		final Author foundAuthor = authorDaoImpl.get(testAuthor.getId());
		assertNotNull(foundAuthor, "retrieved row was not null");
		assertEquals(testAuthor, foundAuthor, "retrieved row has expected data");
	}

	/**
	 * Test that retrieval returns null when given the ID of an author not present.
	 * @throws SQLException on DB error.
	 */
	@DisplayName("Return null if entry not found")
	@Test
	public void testGetNotFound() throws SQLException {
		final Author foundAuthor = authorDaoImpl.get(Integer.MAX_VALUE);
		assertNull(foundAuthor, "author for unused ID was null");
	}

	/**
	 * Test that retrieving all authors works.
	 * @throws SQLException on DB error.
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Author> listOfAuthors = authorDaoImpl.getAll();
		final int authorSize = mySQLSize();
		assertEquals(listOfAuthors.size(), authorSize, "DAO and SQL report same size");
	}
}
