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
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;

/**
 * Tests of the book DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemBookDaoTest {
	/**
	 * Title of a sample book for the tests.
	 */
	private static final String SAMPLE_TITLE = "The Book Title";

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
	 * Sample author name for tests.
	 */
	private static final String SAMPLE_AUTHOR_NAME = "Author Name";

	/**
	 * Connection to the database.
	 */
	private Connection conn;

	/**
	 * Book DAO under test.
	 */
	private BookDao bookDaoImpl;
	/**
	 * Publisher DAO involved in tests.
	 */
	private PublisherDao publisherDaoImpl;
	/**
	 * Author DAO involved in tests.
	 */
	private AuthorDao authorDaoImpl;
	/**
	 * Stored book from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Book testBook;
	/**
	 * Stored author from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Author testAuthor;
	/**
	 * Stored publisher from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Publisher testPublisher;
	/**
	 * The table this DAO accesses.
	 */
	private static final String TABLE = "tbl_book";
	/**
	 * The primary key in the table this DAO accesses.
	 */
	private static final String KEY_FIELD = "bookId";

	/**
	 * Set up the database connection, the DAOs, and the test data before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		bookDaoImpl = new BookDaoImpl(conn);
		publisherDaoImpl = new PublisherDaoImpl(conn);
		authorDaoImpl = new AuthorDaoImpl(conn);
		testAuthor = authorDaoImpl.create(SAMPLE_AUTHOR_NAME);
		testPublisher = publisherDaoImpl.create(SAMPLE_PUBLISHER_NAME, SAMPLE_PUBLISHER_ADDRESS, SAMPLE_PUBLISHER_PHONE);
		testBook = bookDaoImpl.create(SAMPLE_TITLE, testAuthor, testPublisher);
	}

	/**
	 * Remove test data from the database and tear it down after each test.
	 * @throws SQLException on DB error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		authorDaoImpl.delete(testAuthor);
		publisherDaoImpl.delete(testPublisher);
		bookDaoImpl.delete(testBook);
		conn.close();
	}

	private int mySQLSize() throws SQLException {
		final String sql = "SELECT COUNT(" + KEY_FIELD + ") AS size FROM " + TABLE + ";";
		final PreparedStatement prepareStatement = conn.prepareStatement(sql);
		try (ResultSet resultSet = prepareStatement.executeQuery()) {
			resultSet.next();
			return resultSet.getInt("size");
		}
	}

	/**
	 * Test that creating a new book works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void createBookTest() throws SQLException {
		bookDaoImpl.delete(testBook);

		final int previousSize = mySQLSize();

		testBook = bookDaoImpl.create(SAMPLE_TITLE, testAuthor, testPublisher);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize, "creating book adds a row");
		assertEquals(SAMPLE_TITLE, testBook.getTitle(), "created book has expected title");
		assertEquals(testAuthor, testBook.getAuthor(), "created book has expected author");
		assertEquals(testPublisher, testBook.getPublisher(), "created book has expected publisher");
	}

	/**
	 * Test that deleting a book works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void deleteBookTest() throws SQLException {
		final int previousSize = mySQLSize();

		bookDaoImpl.delete(testBook);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize, "deletion removes a row");
		assertNull(bookDaoImpl.get(testBook.getId()), "deleted row is gone from database");
	}

	/**
	 * Test that updating a book works.
	 * @throws SQLException on DB error.
	 */
	@DisplayName("Update Correctly")
	@Test
	public void updateBookTest() throws SQLException {
		final String newTitle = "New Title";
		final String newAuthorName = "New Author Name";
		final String newPublisherName = "New Publisher Name";
		final String newPublisherAddress = "New Address";
		final String newPublisherPhone = "4567891230";

		final Author newAuthor = authorDaoImpl.create(newAuthorName);
		final Publisher newPublisher = publisherDaoImpl.create(newPublisherName,
				newPublisherAddress, newPublisherPhone);
		final Book newBook = new Book(testBook.getId(), newTitle, newAuthor, newPublisher);

		bookDaoImpl.update(newBook);

		final Book updatedbook = bookDaoImpl.get(newBook.getId());

		assertNotNull(updatedbook, "row is present after update");
		assertEquals(newBook, updatedbook, "update propagates data to table");
	}

	/**
	 * Test that updating works even if the book's author is null.
	 * @throws SQLException on DB error.
	 */
	@DisplayName("Update even if author is null")
	@Test
	public void testUpdateWithAuthorNull() throws SQLException {
		final String newTitle = "New Title";
		final String newPublisherName = "New Publisher Name";
		final String newPublisherAddress = "New Address";
		final String newPublisherPhone = "4567891230";

		final Publisher newPublisher = publisherDaoImpl.create(newPublisherName,
				newPublisherAddress, newPublisherPhone);

		final Book newBook = new Book(testBook.getId(), newTitle, null, newPublisher);

		bookDaoImpl.update(newBook);

		final Book updatedBook = bookDaoImpl.get(newBook.getId());

		assertNotNull(updatedBook, "row is present after update");
		assertEquals(newBook, updatedBook, "update propagates data to table");
		assertNull(updatedBook.getAuthor(), "update propagates null author");
	}

	/**
	 * Test that updating works even if publisher is null.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Update even if publisher is null")
	@Test
	public void testUpdateWithPublisherNull() throws SQLException {
		final String newTitle = "New Title";
		final String newAuthorName = "New Author Name";

		final Author newAuthor = authorDaoImpl.create(newAuthorName);

		final Book newBook = new Book(testBook.getId(), newTitle, newAuthor, null);

		bookDaoImpl.update(newBook);

		final Book updatedBook = bookDaoImpl.get(newBook.getId());

		assertNotNull(updatedBook, "row is present after update");
		assertEquals(newBook, updatedBook, "update propagates data to table");
		assertNull(updatedBook.getPublisher(), "update propagates null publisher");
	}

	/**
	 * Test that retrieval works.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGetBook() throws SQLException {
		final Book foundBook = bookDaoImpl.get(testBook.getId());
		assertNotNull(foundBook, "retrieval finds book");
		assertEquals(testBook, foundBook, "retrieval finds expected book");
	}

	/**
	 * Test that retrieving by ID returns null if ID not found.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Return null if entry not found")
	@Test
	public void testGetNotFoundBook() throws SQLException {
		final Book foundBook = bookDaoImpl.get(Integer.MAX_VALUE);
		assertNull(foundBook, "retrieving book with absent ID returns null");
	}

	/**
	 * Test that full-table retrieval works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Book> listOfBooks = bookDaoImpl.getAll();
		final int bookSize = mySQLSize();
		assertEquals(listOfBooks.size(), bookSize, "DAO and SQL agree on number of rows");
	}

}
