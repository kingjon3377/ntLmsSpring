package com.st.novatech.springlms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.dao.InMemoryDBFactory;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;

/**
 * Tests of the librarian service class.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemLibrarianServiceTest {
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
	 * Sample number of copies for tests.
	 */
	private static int noOfCopies = 50;

	/**
	 * The connection to the database.
	 */
	private Connection db;

	/**
	 * Administrator service involved in tests.
	 */
	private AdministratorService adminService;
	/**
	 * Librarian service under test.
	 */
	private LibrarianService libService;

	/**
	 * Set up database connection, services, and test data before each test.
	 *
	 * @throws SQLException         on database error
	 * @throws TransactionException on error caught by a service
	 * @throws IOException          on I/O error reading database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, TransactionException, IOException {
		db = InMemoryDBFactory.getConnection("library");
		adminService = new AdministratorServiceImpl(db);
		libService = new LibrarianServiceImpl(db);
		testBook = adminService.createBook(SAMPLE_TITLE, null, null);
		testBranch = adminService.createBranch(SAMPLE_BRANCH_NAME,
				SAMPLE_BRANCH_ADDRESS);
		// due date is two weeks from now
		libService.setBranchCopies(testBranch, testBook, noOfCopies);
	}

	/**
	 * Delete test data and tear down database connection after each test.
	 * @throws SQLException on database error
	 * @throws TransactionException on error caught by a service
	 */
	@AfterEach
	public void tearDown() throws SQLException, TransactionException {
		// FIXME?: WARNING maybe something that doesn't call the method we are trying to test
		adminService.deleteBook(testBook);
		adminService.deleteBranch(testBranch);
		libService.setBranchCopies(testBranch, testBook, 0);
		db.close();
	}

	/**
	 * Test that updating a null branch will throw a NPE.
	 *
	 * <p>FIXME: service should catch this and turn it into a TransactionException
	 * @throws TransactionException on error caught by the service
	 */
	@DisplayName("throws null pointer exception if null is passed as a parameter for update branch")
	@Test
	public void updateBranchTest() throws TransactionException {
		assertThrows(NullPointerException.class, () -> libService.updateBranch(null),
				"Expecting to throw null pointer exception");
	}

	/**
	 * Test that setting branch copies where no record already exists adds a row to
	 * the database.
	 *
	 * @throws TransactionException on error caught by the service
	 */
	@DisplayName("Adds noOfCopies to the book copies table if it doesnt already exist")
	@Disabled("Currently failing despite looking correct by inspection")
	@Test
	public void setBranchCopiesNonExistingTest() throws TransactionException {
		final Map<Branch, Map<Book, Integer>> previousListOfCopies = libService.getAllCopies();
		assertTrue(previousListOfCopies.containsKey(testBranch),
				"already have records for the test branch");

		final int customNoOfCopies = 99;
		libService.setBranchCopies(testBranch, testBook, customNoOfCopies);
		libService.commit();
		final Map<Branch, Map<Book, Integer>> currentListOfCopies = libService.getAllCopies();
		assertTrue(currentListOfCopies.containsKey(testBranch),
				"still have records for the test branch");

		assertEquals(customNoOfCopies,
				currentListOfCopies.get(testBranch).get(testBook).intValue(),
				"new copy count propagated to the database");
	}
}
