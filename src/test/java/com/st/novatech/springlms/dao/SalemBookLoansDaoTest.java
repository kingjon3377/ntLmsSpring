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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;

/**
 * Tests of the book-loans DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemBookLoansDaoTest {
	/**
	 * Connection to the database.
	 */
	private static Connection conn = null;

	/**
	 * The table this DAO accesses.
	 */
	private static final String TABLE = "tbl_book_loans";
	/**
	 * A key field in the table this DAO accesses.
	 */
	private static final String KEY_FIELD = "bookId";

	/**
	 * Sample book title for tests.
	 */
	private static final String SAMPLE_TITLE = "The Book Title";

	/**
	 * Sample branch name for tests.
	 */
	private static final String SAMPLE_BRANCH_NAME = "The Branch Name";
	/**
	 * Sample branch address for tests.
	 */
	private static final String SAMPLE_BRANCH_ADDRESS = "601 New Jersey Ave, Washington, DC 20001";

	/**
	 * Sample borrower name for tests.
	 */
	private static final String SAMPLE_PATRON_NAME = "The Borrower Name";
	/**
	 * Sample borrower address for tests.
	 */
	private static final String SAMPLE_PATRON_ADDRESS = "650 New Jersey Ave, Washington, DC 20001";
	/**
	 * Sample borrower phone for tests.
	 */
	private static final String SAMPLE_PATRON_PHONE = "1234567890";

	/**
	 * Sample checkout date for tests. Time is 00:00 because "time" gets converted
	 * to "date" by JDBC.
	 */
	private final LocalDateTime dateOut = LocalDate.now().atTime(00,00);
	/**
	 * Sample due date for tests.
	 */
	private final LocalDate dueDate = LocalDate.now().plusWeeks(1);

	/**
	 * Book DAO involved in tests.
	 */
	private static BookDao bookDaoImpl;
	/**
	 * Branch DAO involved in tests.
	 */
	private static LibraryBranchDao branchDaoImpl;
	/**
	 * Borrower DAO involved in tests.
	 */
	private static BorrowerDao borrowerDaoImpl;
	/**
	 * Loans DAO under test.
	 */
	private static BookLoansDao loansDaoImpl;

	/**
	 * Stored book from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Book testBook;
	/**
	 * Stored branch from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Branch testBranch;
	/**
	 * Stored borrower from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Borrower testBorrower;
	/**
	 * Stored loan from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Loan testLoan;

	/**
	 * Set up the database connection, the DAOs, and the test data before each test.
	 * @throws SQLException on DB error
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		bookDaoImpl = new BookDaoImpl(conn);
		branchDaoImpl = new LibraryBranchDaoImpl(conn);
		borrowerDaoImpl = new BorrowerDaoImpl(conn);
		loansDaoImpl = new BookLoansDaoImpl(conn);
		testBook = bookDaoImpl.create(SAMPLE_TITLE, null, null);
		testBranch = branchDaoImpl.create(SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);
		testBorrower = borrowerDaoImpl.create(SAMPLE_PATRON_NAME, SAMPLE_PATRON_ADDRESS, SAMPLE_PATRON_PHONE);
		testLoan = loansDaoImpl.create(testBook, testBorrower, testBranch, dateOut, dueDate);
	}

	/**
	 * Delete test data from database and tear it down after each test.
	 * @throws SQLException on DB error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		bookDaoImpl.delete(testBook);
		branchDaoImpl.delete(testBranch);
		borrowerDaoImpl.delete(testBorrower);
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
	 * Test that creating a loan works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void createLoanTest() throws SQLException {
		loansDaoImpl.delete(testLoan);

		final int previousSize = mySQLSize();

		testLoan = loansDaoImpl.create(testBook, testBorrower, testBranch, dateOut, dueDate);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize, "Creating loan creates record");
		assertEquals(testBook, testLoan.getBook(), "created loan has expected book");
		assertEquals(testBorrower, testLoan.getBorrower(), "created loan has expected borrower");
		assertEquals(testBranch, testLoan.getBranch(), "created loan has expected branch");
		assertEquals(dateOut, testLoan.getDateOut(), "created loan has expected date out");
		assertEquals(dueDate, testLoan.getDueDate(), "created loan has expected due date");
	}

	/**
	 * Test tht deleting a loan works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void deleteLoanTest() throws SQLException {
		final int previousSize = mySQLSize();

		loansDaoImpl.delete(testLoan);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize, "deleting loan removes a row");
		assertNull(loansDaoImpl.get(testLoan.getBook(), testLoan.getBorrower(),
				testLoan.getBranch()), "deleted row is gone from database");
	}

	/**
	 * Test that updating a loan works.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Update Correctly")
	@Test
	public void updateLoansTest() throws SQLException {
		final LocalDateTime newDateOut = LocalDate.now().plusDays(5).atTime(00,00);
		final LocalDate newDueDate = LocalDate.now().plusDays(5).plusWeeks(1);

		final Loan newLoan = new Loan(testLoan.getBook(), testLoan.getBorrower(), testLoan.getBranch(), newDateOut, newDueDate);

		loansDaoImpl.update(newLoan);

		final Loan updatedloans = loansDaoImpl.get(newLoan.getBook(), newLoan.getBorrower(), newLoan.getBranch());

		assertNotNull(updatedloans, "row still present after update");
		assertEquals(newLoan, updatedloans, "update propagates data to database");
	}

	/**
	 * Test that updating a loan works even if its date out is null.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Update even if dateOut is null")
	@Test
	public void updateWithDateOutNullTest() throws SQLException {
		final LocalDate newDueDate = LocalDate.now().plusDays(5).plusWeeks(1);

		final Loan newLoan = new Loan(testLoan.getBook(), testLoan.getBorrower(), testLoan.getBranch(), null, newDueDate);

		loansDaoImpl.update(newLoan);

		final Loan updatedLoan = loansDaoImpl.get(newLoan.getBook(), newLoan.getBorrower(), newLoan.getBranch());

		assertNotNull(updatedLoan, "row still present after update");
		assertEquals(newLoan, updatedLoan, "update propagates data to database");
		assertNull(updatedLoan.getDateOut(), "update propagates null date out");
	}

	/**
	 * Test that updating a loan works even if its due date is null.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Update even if dueDate is null")
	@Test
	public void updateWithDueDateNullTest() throws SQLException {
		final LocalDateTime newDateOut = LocalDate.now().plusDays(5).atTime(00,00);

		final Loan newLoan = new Loan(testLoan.getBook(), testLoan.getBorrower(), testLoan.getBranch(), newDateOut, null);

		loansDaoImpl.update(newLoan);

		final Loan updatedLoan = loansDaoImpl.get(newLoan.getBook(), newLoan.getBorrower(), newLoan.getBranch());

		assertNotNull(updatedLoan, "row still present after update");
		assertEquals(newLoan, updatedLoan, "update propagates data to database");
		assertNull(updatedLoan.getDueDate(), "update propagates null due date");
	}

	/**
	 * Test that basic retrieval works.
	 * @throws SQLException on DB error
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGetLoan() throws SQLException {
		final Loan foundLoan = loansDaoImpl.get(testLoan.getBook(), testLoan.getBorrower(), testLoan.getBranch());
		assertNotNull(foundLoan, "retrieval finds loan");
		assertEquals(testLoan, foundLoan, "retrieval finds expected loan");
	}

	/**
	 * Test that full-table retrieval works.
	 * @throws SQLException on DB error
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Loan> listOfLoans = loansDaoImpl.getAll();
		final int loanSize = mySQLSize();
		assertEquals(listOfLoans.size(), loanSize, "DAO and SQL agree on number of loans");
	}

}
