package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;

/**
 * Test of the loans DAO.
 *
 * @author Jonathan Lovelace
 *
 */
class BookLoansDaoTest {
	/**
	 * The DAO being tested.
	 */
	private BookLoansDao testee;
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
		testee = new BookLoansDaoImpl(db);
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
	 * Test of loan creation.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testCreate() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Book book = bookDao.create("borrowed book title", null, null);
		final Branch branch = branchDao.create("library name", "");
		final Borrower borrower = borrowerDao.create("borrower name", "", "");
		final LocalDateTime timeOut = LocalDateTime.now();
		final LocalDate due = LocalDate.now();
		final Loan expected = new Loan(book, borrower, branch, timeOut, due);
		final Loan loan = testee.create(book, borrower, branch, timeOut, due);
		assertEquals(expected, loan, "Creating loan returns expected result");
		assertThrows(SQLException.class,
				() -> testee.create(book, borrower, branch, timeOut, due),
				"Can't create duplicate loan");
	}

	/**
	 * Test of loan update.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testUpdate() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Book book = bookDao.create("borrowed book title", null, null);
		final Branch branch = branchDao.create("library name", "");
		final Borrower borrower = borrowerDao.create("borrower name", "", "");
		final LocalDateTime timeOut = LocalDateTime.now();
		final LocalDate due = LocalDate.now();
		final Loan original = new Loan(book, borrower, branch, timeOut, due);
		final Loan loan = testee.create(book, borrower, branch, timeOut, due);
		assertEquals(original, loan, "Loan is as expected before update");
		loan.setDateOut(timeOut.minusDays(2));
		loan.setDueDate(due.plusWeeks(1));
		testee.update(loan);
		assertNotEquals(original, testee.get(book, borrower, branch),
				"Loan has changed after update");
	}

	/**
	 * Test of deletion.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testDelete() throws SQLException {
		assertEquals(0, testee.getAll().size(), "Empty table before adding");
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Book book = bookDao.create("book title", null, null);
		final Branch branch = branchDao.create("branch name", "");
		final Borrower borrower = borrowerDao.create("patron name", "", "");
		final LocalDateTime timeOut = LocalDateTime.now();
		final LocalDate due = LocalDate.now();
		final Loan loan = testee.create(book, borrower, branch, timeOut, due);
		assertEquals(1, testee.getAll().size(), "One row after adding");
		testee.delete(loan);
		assertEquals(0, testee.getAll().size(), "Empty table after removing loan");
	}

	/**
	 * Test of individual-row retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGet() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Book book = bookDao.create("book title", null, null);
		final Branch branch = branchDao.create("branch name", "");
		final Borrower borrower = borrowerDao.create("patron name", "", "");
		final LocalDateTime timeOut = LocalDate.now().atStartOfDay();
		final LocalDate due = LocalDate.now();
		assertNull(testee.get(book, borrower, branch),
				"Null result for loan not in table yet");
		final Loan expected = new Loan(book, borrower, branch, timeOut, due);
		testee.create(book, borrower, branch, timeOut, due);
		assertEquals(expected, testee.get(book, borrower, branch),
				"Result of retrieval is as expected");
	}

	/**
	 * Test of full-table retrieval.
	 * @throws SQLException if something goes wrong
	 */
	@Test
	public final void testGetAll() throws SQLException {
		final BookDao bookDao = new BookDaoImpl(db);
		final LibraryBranchDao branchDao = new LibraryBranchDaoImpl(db);
		final BorrowerDao borrowerDao = new BorrowerDaoImpl(db);
		final Book bookOne = bookDao.create("book title", null, null);
		final Book bookTwo = bookDao.create("book two", null, null);
		final Branch branch = branchDao.create("branch name", "");
		final Borrower borrower = borrowerDao.create("patron name", "", "");
		final LocalDateTime timeOut = LocalDate.now().atStartOfDay();
		final LocalDate due = LocalDate.now();
		final Set<Loan> expected = new HashSet<>(
				Arrays.asList(new Loan(bookOne, borrower, branch, timeOut, due),
						new Loan(bookTwo, borrower, branch, null, null)));
		testee.create(bookOne, borrower, branch, timeOut, due);
		testee.create(bookTwo, borrower, branch, null, null);
		assertEquals(expected, new HashSet<>(testee.getAll()),
				"All loans are returned by getAll");
	}
}
