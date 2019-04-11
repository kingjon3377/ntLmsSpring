package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;

/**
 * Test of the borrower DAO.
 * @author Jonathan Lovelace
 *
 */
class BorrowerDaoTest {
	/**
	 * The DAO being tested.
	 */
	private BorrowerDao testee;
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
		testee = new BorrowerDaoImpl(db);
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
	 * Test creation.
	 * @throws SQLException if something goes wrong.
	 */
	@Test
	public final void testCreate() throws SQLException {
		final Borrower expected = new Borrower(1, "borrower name",
				"borrower address", "borrower phone");
		final Borrower borrower = testee.create("borrower name", "borrower address",
				"borrower phone");
		assertEquals(expected, borrower, "Creation works as expected");
		final Borrower second = new Borrower(2, "second name", "", "");
		assertEquals(second, testee.create("second name", "", ""),
				"Empty strings work");
		try (Statement statement = db.createStatement();
				ResultSet rs = statement.executeQuery(
						"SELECT COUNT(*) AS `count` FROM `tbl_borrower`")) {
			rs.next();
			assertEquals(2, rs.getInt(1), "Table has expected number of rows");
		}
	}

	/**
	 * Test update.
	 * @throws SQLException if something goes wrong.
	 */
	@Test
	public final void testUpdate() throws SQLException {
		final String beforePhone = "before phone";
		final Borrower before = new Borrower(1, "before name", "before address",
				beforePhone);
		final String middleName = "middle name";
		final Borrower middle = new Borrower(1, middleName, "before address",
				beforePhone);
		final Borrower later = new Borrower(1, middleName, "later address",
				beforePhone);
		final Borrower after = new Borrower(1, middleName, "later address",
				"after phone");
		final Borrower borrower = testee.create("before name", "before address",
				beforePhone);
		assertEquals(before, testee.get(1), "initial state as expected");
		borrower.setName(middleName);
		testee.update(borrower);
		assertEquals(middle, testee.get(1), "update propagated to database");
		borrower.setAddress("later address");
		testee.update(borrower);
		assertEquals(later, testee.get(1), "second update propagated to database");
		borrower.setPhone("after phone");
		testee.update(borrower);
		assertEquals(after, testee.get(1), "third update propagated to database");
	}

	/**
	 * Test delete.
	 * @throws SQLException if something goes wrong.
	 */
	@Test
	public final void testDelete() throws SQLException {
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_borrower` (`name`, `address`, `phone`) VALUES (?, NULL, NULL)")) {
			for (final String name : Arrays.asList("first borrower",
					"second borrower", "third borrower")) {
				statement.setString(1, name);
				statement.executeUpdate();
			}
		}
		assertEquals(3, testee.getAll().size());
		testee.delete(new Borrower(2, "second borrower", "", ""));
		assertEquals(
				new HashSet<>(
						Arrays.asList(new Borrower(1, "first borrower", "", ""),
								new Borrower(3, "third borrower", "", ""))),
				new HashSet<>(testee.getAll()),
				"Remaining values are as expected after deletion");
	}

	/**
	 * Test retrieval of a single row.
	 * @throws SQLException if something goes wrong.
	 */
	@Test
	public final void testGet() throws SQLException {
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_borrower` (`name`, `address`, `phone`) VALUES (?, ?, ?)")) {
			statement.setString(1, "patron one");
			statement.setString(2, "first address");
			statement.setString(3, "first phone");
			statement.executeUpdate();
			statement.setString(1, "patron two");
			statement.setString(2, "second address");
			statement.setString(3, "second phone");
			statement.executeUpdate();
			statement.setString(1, "patron three");
			statement.setNull(2, Types.VARCHAR);
			statement.setNull(3, Types.VARCHAR);
			statement.executeUpdate();
		}
		assertEquals(new Borrower(1, "patron one", "first address", "first phone"),
				testee.get(1), "get() returns expected borrower");
		assertEquals(new Borrower(3, "patron three", "", ""), testee.get(3),
				"get() translates nulls to empty strings");
		assertEquals(new Borrower(2, "patron two", "second address", "second phone"),
				testee.get(2), "get() returns expected borrower");
		assertNull(testee.get(5), "get() returns null if no such row");
	}

	/**
	 * Test full-table retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetAll() throws SQLException {
		try (PreparedStatement statement = db.prepareStatement(
				"INSERT INTO `tbl_borrower` (`name`, `address`, `phone`) VALUES (?, ?, ?)")) {
			statement.setString(1, "borrower one");
			statement.setString(2, "address one");
			statement.setString(3, "phone one");
			statement.executeUpdate();
			statement.setString(1, "borrower two");
			statement.setString(2, "address two");
			statement.setString(3, "phone two");
			statement.executeUpdate();
			statement.setString(1, "borrower three");
			statement.setNull(2, Types.VARCHAR);
			statement.setNull(3, Types.VARCHAR);
			statement.executeUpdate();
		}
		assertEquals(
				new HashSet<>(Arrays.asList(
						new Borrower(1, "borrower one", "address one", "phone one"),
						new Borrower(2, "borrower two", "address two", "phone two"),
						new Borrower(3, "borrower three", "", ""))),
				new HashSet<>(testee.getAll()), "getAll() returns expected rows");
	}

	/**
	 * Test that when the DAO removes a borrower, all that borrower's loans are also
	 * removed.
	 *
	 * @throws SQLException if something goes very wrong
	 */
	@Test
	public final void testDeleteLoansCascade() throws SQLException {
		final Borrower toRemove = testee.create("borrower to remove", "", "");
		final Borrower toKeep = testee.create("borrower to keep", "", "");
		final BookLoansDao loansDao = new BookLoansDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final Branch branch = branchDao.create("branch name", "");
		final BookDao bookDao = new BookDaoImpl(db);
		final Book book = bookDao.create("book title", null, null);
		loansDao.create(book, toRemove, branch, null, null);
		loansDao.create(book, toKeep, branch, null, null);
		assertEquals(2, loansDao.getAll().size(),
				"Two outstanding loans before deletion");
		testee.delete(toRemove);
		assertEquals(1, loansDao.getAll().size(),
				"Loan of book to deleted borrower was also removed");
	}
}
