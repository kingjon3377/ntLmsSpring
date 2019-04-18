package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;
/**
 * Test of the copies DAO.
 * @author Jonathan Lovelace
 */
class CopiesDaoTest {
	/**
	 * The DAO being tested.
	 */
	private CopiesDao testee;
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
		testee = new CopiesDaoImpl(db);
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
	 * Test single-row retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetCopies() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Book firstBook = bookDao.create("book one", null, null);
		final Book secondBook = bookDao.create("book two", null, null);
		final Branch firstBranch = branchDao.create("branch one", "address one");
		final Branch secondBranch = branchDao.create("branch two", "");
		assertEquals(0, testee.getCopies(firstBranch, firstBook),
				"No copies when table is empty");
		testee.setCopies(firstBranch, firstBook, 2);
		assertEquals(2, testee.getCopies(firstBranch, firstBook),
				"Expected number of copies returned");
		assertEquals(0, testee.getCopies(firstBranch, secondBook),
				"Copy counts are not shared between books");
		assertEquals(0, testee.getCopies(secondBranch, firstBook),
				"Copy counts are not shared between branches");
	}

	/**
	 * Test single-row insertion/update.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testSetCopies() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Book firstBook = bookDao.create("book one", null, null);
		final Book secondBook = bookDao.create("book two", null, null);
		final Branch firstBranch = branchDao.create("branch one", "address one");
		final Branch secondBranch = branchDao.create("branch two", "");
		try (PreparedStatement count = db
				.prepareStatement("SELECT COUNT(*) FROM `tbl_book_copies`");
				PreparedStatement sum = db.prepareStatement(
						"SELECT SUM(`noOfCopies`) FROM `tbl_book_copies`")) {
			try (ResultSet rs = count.executeQuery()) {
				rs.next();
				assertEquals(0, rs.getInt(1), "No rows before inserting data");
			}
			testee.setCopies(firstBranch, firstBook, 2);
			testee.setCopies(firstBranch, secondBook, 3);
			testee.setCopies(secondBranch, firstBook, 5);
			try (ResultSet rs = count.executeQuery()) {
				rs.next();
				assertEquals(3, rs.getInt(1), "All expected rows present");
			}
			try (ResultSet rs = sum.executeQuery()) {
				rs.next();
				assertEquals(10, rs.getInt(1), "Expected number of copies present");
			}
			testee.setCopies(firstBranch, firstBook, 0);
			try (ResultSet rs = count.executeQuery()) {
				rs.next();
				assertEquals(2, rs.getInt(1), "Setting count to 0 removes row");
			}
		}
	}

	/**
	 * Test getting all copies for a given branch.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetAllBranchCopies() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Book firstBook = bookDao.create("first book", null, null);
		final Book secondBook = bookDao.create("second book", null, null);
		final Branch firstBranch = branchDao.create("first branch", "first address");
		final Branch secondBranch = branchDao.create("second branch", "");
		testee.setCopies(firstBranch, firstBook, 2);
		testee.setCopies(firstBranch, secondBook, 3);
		testee.setCopies(secondBranch, firstBook, 5);
		final Map<Book, Integer> expected = new HashMap<>();
		expected.put(firstBook, 2);
		expected.put(secondBook, 3);
		assertEquals(expected, testee.getAllBranchCopies(firstBranch),
				"Expected book copies returned");
		expected.clear();
		expected.put(firstBook, 5);
		assertEquals(expected, testee.getAllBranchCopies(secondBranch),
				"Expected book copies returned");
	}

	/**
	 * Test getting all copies of a given book.
	 * @throws SQLException if something goes wrong.
	 */
	@Test
	public final void testGetAllBookCopies() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Book firstBook = bookDao.create("first book", null, null);
		final Book secondBook = bookDao.create("second book", null, null);
		final Branch firstBranch = branchDao.create("first branch", "first address");
		final Branch secondBranch = branchDao.create("second branch", "");
		testee.setCopies(firstBranch, firstBook, 2);
		testee.setCopies(firstBranch, secondBook, 3);
		testee.setCopies(secondBranch, firstBook, 5);
		final Map<Branch, Integer> expected = new HashMap<>();
		expected.put(firstBranch, 2);
		expected.put(secondBranch, 5);
		assertEquals(expected, testee.getAllBookCopies(firstBook),
				"Expected branch copies returned");
		expected.clear();
		expected.put(firstBranch, 3);
		assertEquals(expected, testee.getAllBookCopies(secondBook),
				"Expected branch copies returned");
	}

	/**
	 * Test full-table retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetAllCopies() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Book firstBook = bookDao.create("first book", null, null);
		final Book secondBook = bookDao.create("second book", null, null);
		final Branch firstBranch = branchDao.create("first branch", "first address");
		final Branch secondBranch = branchDao.create("second branch", "");
		testee.setCopies(firstBranch, firstBook, 2);
		testee.setCopies(firstBranch, secondBook, 3);
		testee.setCopies(secondBranch, firstBook, 5);
		final Map<Branch, Map<Book, Integer>> expected = new HashMap<>();
		final Map<Book, Integer> first = new HashMap<>();
		first.put(firstBook, 2);
		first.put(secondBook, 3);
		expected.put(firstBranch, first);
		expected.put(secondBranch, Collections.singletonMap(firstBook, 5));
		assertEquals(expected, testee.getAllCopies(), "Expected values returned");
	}
}
