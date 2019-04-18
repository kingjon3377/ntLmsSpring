package com.st.novatech.springlms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.dao.InMemoryDBFactory;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;

/**
 * Tests of the administrator service class.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemAdministratorServiceTest {

	/**
	 * Sample book title used in tests.
	 */
	private static final String SAMPLE_TITLE = "The Book Title";

	/**
	 * Sample branch name used in tests.
	 */
	private static final String SAMPLE_BRANCH_NAME = "The Branch Name";
	/**
	 * Sample branch address used in tests.
	 */
	private static final String SAMPLE_BRANCH_ADDRESS = "601 New Jersey Ave, Washington, DC 20001";

	/**
	 * Sample borrower name used in tests.
	 */
	private static final String SAMPLE_PATRON_NAME = "The Borrower Name";
	/**
	 * Sample borrower address used in tests.
	 */
	private static final String SAMPLE_PATRON_ADDRESS = "650 New Jersey Ave, Washington, DC 20001";
	/**
	 * Sample borrower phone used in tests.
	 */
	private static final String SAMPLE_PATRON_PHONE = "1234567890";

	/**
	 * Due date for testing purposes, two weeks from today.
	 */
	private static final LocalDate OFFICIAL_DUE_DATE = LocalDate.now().plusWeeks(2);

	/**
	 * Number of copies for testing purposes.
	 */
	private static final int NUM_COPIES = 50;

	/**
	 * Stored borrower from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Borrower testBorrower;
	/**
	 * Stored book from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Book testBook;
	/**
	 * Stored loan from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Loan testLoan;
	/**
	 * Stored branch from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Branch testBranch;

	/**
	 * The connection to the database.
	 */
	private Connection db;

	/**
	 * Administrator service object under test.
	 */
	private AdministratorService adminService;
	/**
	 * Borrower service object involved in tests.
	 */
	private BorrowerService borrowerService;
	/**
	 * Librarian service object involved in tests.
	 */
	private LibrarianService libraryService;

	/**
	 * Set up the database connection, service objects, and test data for each test.
	 *
	 * @throws SQLException         on database errors
	 * @throws IOException          on I/O error reading database schema from file
	 * @throws TransactionException on error caught by the service layer
	 */
	@BeforeEach
	public void init() throws SQLException, TransactionException, IOException {
		db = InMemoryDBFactory.getConnection("library");
		adminService = new AdministratorServiceImpl(db);
		borrowerService = new BorrowerServiceImpl(db);
		libraryService = new LibrarianServiceImpl(db);
		testBorrower = adminService.createBorrower(SAMPLE_PATRON_NAME, SAMPLE_PATRON_ADDRESS,
				SAMPLE_PATRON_PHONE);
		testBook = adminService.createBook(SAMPLE_TITLE, null, null);
		testBranch = adminService.createBranch(SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);
		libraryService.setBranchCopies(testBranch, testBook, NUM_COPIES);
		testLoan = borrowerService.borrowBook(testBorrower, testBook, testBranch,
				LocalDateTime.now(), OFFICIAL_DUE_DATE);
	}

	/**
	 * Clean up after each test.
	 * @throws SQLException on database error
	 * @throws TransactionException on error caught by the service layer
	 */
	@AfterEach
	public void tearThis() throws SQLException, TransactionException {
		borrowerService.returnBook(testBorrower, testBook, testBranch,
				LocalDate.now());
		libraryService.setBranchCopies(testBranch, testBook, 0);
		adminService.deleteBorrower(testBorrower);
		adminService.deleteBook(testBook);
		adminService.deleteBranch(testBranch);
		db.close();
	}

	/**
	 * Test that overriding due dates works.
	 * @throws SQLException on error in DAO or below
	 * @throws TransactionException on error caught by service class
	 */
	@DisplayName("Override due date correctly")
	@Test
	public void overrideDueDateForLoanTest()
			throws SQLException, TransactionException {
		final boolean success = adminService.overrideDueDateForLoan(testBook,
				testBorrower, testBranch, OFFICIAL_DUE_DATE.plusWeeks(1));

		final List<Loan> listOfLoansWithOneLoan = getListThatMatches(testBook,
				testBorrower, testBranch);

		assertEquals(1, listOfLoansWithOneLoan.size(), "only one lon");

		final Loan foundLoan = listOfLoansWithOneLoan.get(0);

		assertTrue(success, "override operation reported success");
		assertEquals(testLoan.getDueDate().plusWeeks(1), foundLoan.getDueDate(),
				"new due date is as expected");
	}

	/**
	 * Test that overriding due dates fails if no such loan exists.
	 * @throws SQLException on error in DAO or below
	 * @throws TransactionException on error caught by service class
	 */
	@DisplayName("Override due date fails because there is no such loan")
	@Test
	public void overrideDueDateForNullLoanTest()
			throws SQLException, TransactionException {
		final Book nonExistingBook = new Book(Integer.MAX_VALUE, "Some Title", null,
				null);
		final boolean success = adminService.overrideDueDateForLoan(nonExistingBook,
				testBorrower, testBranch, OFFICIAL_DUE_DATE.plusWeeks(1));

		final List<Loan> listOfLoansWithNoLoan = getListThatMatches(nonExistingBook,
				testBorrower, testBranch);

		assertEquals(0, listOfLoansWithNoLoan.size(), "no matching loans when loan does not exist");

		final List<Loan> listOfLoansWithOneLoan = getListThatMatches(testBook,
				testBorrower, testBranch);

		assertEquals(1, listOfLoansWithOneLoan.size(), "only one matching loan");

		final Loan foundLoan = listOfLoansWithOneLoan.get(0);

		assertFalse(success, "override operation reported failure");
		assertEquals(testLoan.getDueDate(), foundLoan.getDueDate(), "due date didn't change");
	}

	private List<Loan> getListThatMatches(final Book book, final Borrower borrower,
			final Branch branch) throws TransactionException {
		final List<Loan> tempListOfAllLoans = adminService.getAllLoans();
		return tempListOfAllLoans
				.parallelStream()
				.filter(l -> l.getBook().equals(book)
						&& l.getBorrower().equals(borrower)
						&& l.getBranch().equals(branch))
				.collect(Collectors.toList());
	}
}
