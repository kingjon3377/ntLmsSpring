package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Publisher;

/**
 * A test case for the book DAO class.
 *
 * @author Jonathan Lovelace
 */
public final class BookDaoTest {
	/**
	 * The DAO being tested.
	 */
	private BookDao testee;

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
		testee = new BookDaoImpl(db);
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
		final AuthorDao authorDao = new AuthorDaoImpl(db);
		final PublisherDao publisherDao = new PublisherDaoImpl(db);
		final Author author = authorDao.create("author name");
		final Publisher publisher = publisherDao.create("publisher name", "", "");
		final Book book = testee.create("book title", null, null);
		book.setAuthor(author);
		book.setPublisher(publisher);
		book.setTitle("changed title");
		testee.update(book);
		try (Statement statement = db.createStatement();
				ResultSet rs = statement.executeQuery("SELECT * FROM `tbl_book`")) {
			rs.next();
			assertEquals(author.getId(), rs.getInt("authId"),
					"Update records changed author");
			assertEquals(publisher.getId(), rs.getInt("pubId"),
					"Update records changed publisher");
			assertEquals("changed title", rs.getString("title"),
					"Update records changed title");
			assertFalse(rs.next(), "Only one row so far");
		}
	}

	/**
	 * Test that deleting authors through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDelete() throws SQLException {
		final AuthorDao authorDao = new AuthorDaoImpl(db);
		final PublisherDao publisherDao = new PublisherDaoImpl(db);
		final Author author = authorDao.create("author name");
		final Publisher publisher = publisherDao.create("publisher name", "", "");
		final Book toDelete = new Book(1, "book to delete", author, publisher);
		final List<Book> expected = Arrays.asList(
				new Book(2, "first book", author, publisher),
				new Book(3, "second book", null, publisher),
				new Book(4, "third book", author, null));
		testee.create(toDelete.getTitle(), toDelete.getAuthor(), toDelete.getPublisher());
		for (final Book book : expected) {
			assertEquals(book,
					testee.create(book.getTitle(), book.getAuthor(),
							book.getPublisher()),
					"Created book matches what we intended");
		}
		assertEquals(4, testee.getAll().size(), "Has correct row count before delete");
		testee.delete(toDelete);
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"Deleted book is gone");
		assertNull(testee.get(1), "Deleted book is gone");
	}

	/**
	 * Test that when the DAO removes a book, all copies of that book are
	 * removed.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDeleteCopiesCascade() throws SQLException {
		final Book toRemove = testee.create("book to remove", null, null);
		final Book toKeep = testee.create("book to keep", null, null);
		final CopiesDao copiesDao = new CopiesDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Branch branch = branchDao.create("branch name", "");
		copiesDao.setCopies(branch, toKeep, 3);
		copiesDao.setCopies(branch, toRemove, 2);
		assertEquals(5, copiesDao.getAllBranchCopies(branch).values().stream()
				.reduce(0, Integer::sum),
				"Expected number of copies in the database");
		testee.delete(toRemove);
		assertEquals(3, copiesDao.getAllBranchCopies(branch).values().stream()
				.reduce(0, Integer::sum),
				"Expected number of copies in the database after deleting book");
	}
	/**
	 * Test that when the DAO removes a book, all copies of that book are
	 * removed.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testDeleteLoansCascade() throws SQLException {
		final Book toRemove = testee.create("book to remove", null, null);
		final Book toKeep = testee.create("book to keep", null, null);
		final BookLoansDao loansDao = new BookLoansDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Branch branch = branchDao.create("branch name", "");
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Borrower borrower = borrowerDao.create("borrower", "", "");
		loansDao.create(toKeep, borrower, branch, null, null);
		loansDao.create(toRemove, borrower, branch, null, null);
		assertEquals(2, loansDao.getAll().size(),
				"Two outstanding loans before deletion");
		testee.delete(toRemove);
		assertEquals(1, loansDao.getAll().size(),
				"Loan of deleted book was also removed");
	}
	/**
	 * Test that getting an author through the DAO works properly.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testGet() throws SQLException {
		final List<Book> expected = Arrays.asList(
				new Book(1, "one book", null, null),
				new Book(2, "two book", null, null),
				new Book(3, "red book", null, null),
				new Book(4, "blue book", null, null));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_book` (`title`, `authId`, `pubId`) VALUES(?, ?, ?)")) {
			for (final Book book : expected) {
				statement.setString(1, book.getTitle());
				statement.setNull(2, Types.INTEGER);
				statement.setNull(3, Types.INTEGER);
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
				"Before adding any books, getAll() returns empty list");
		final List<Book> expected = Arrays.asList(new Book(1, "first book", null, null),
				new Book(2, "second book", null, null), new Book(3, "third book", null, null));
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_book` (`title`, `authId`, `pubId`) VALUES(?, ?, ?)")) {
			for (final Book book : expected) {
				statement.setString(1, book.getTitle());
				statement.setNull(2, Types.INTEGER);
				statement.setNull(3, Types.INTEGER);
				statement.executeUpdate();
			}
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAll()),
				"getAll() returns expected books");
	}
	/**
	 * Test that creating an author through the DAO works as expected.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public void testCreate() throws SQLException {
		final Book book = testee.create("test book", null, null);
		assertEquals("test book", book.getTitle(),
				"created book has expected title");
		assertEquals(1, book.getId(), "created book has expected ID");
		final Book another = testee.create("test book", null, null);
		assertEquals(2, another.getId(), "second book has expected ID");
		final Book third = testee.create("another book", null, null);
		assertEquals(3, third.getId(), "third book has expected ID");
		assertEquals("another book", third.getTitle(),
				"third book has expected title");
		try (Statement statement = db.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT COUNT(*) AS `count` FROM `tbl_book`")) {
			rs.next();
			assertEquals(3, rs.getInt(1), "table has expected number of rows");
		}
		assertThrows(SQLException.class, () -> testee.create(null, null, null));
	}
}
