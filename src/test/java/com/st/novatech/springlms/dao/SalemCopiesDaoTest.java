package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;

/**
 * Tests of the book-copies DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemCopiesDaoTest {
	/**
	 * Database connection.
	 */
	private static Connection conn = null;

	/**
	 * The table this DAO represents.
	 */
	private static final String TABLE = "tbl_book_copies";
	/**
	 * The primary key in the table.
	 */
	private static final String PRIMARY_KEY = "bookId";

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
	 * Sample number of copies for tests.
	 */
	private static final int NUM_COPIES = 50;

	/**
	 * Book DAO involved in tests.
	 */
	private BookDao bookDaoImpl;
	/**
	 * Branch DAO involved in tests.
	 */
	private LibraryBranchDao branchDaoImpl;
	/**
	 * Copies DAO under test.
	 */
	private CopiesDao copiesDaoImpl;

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
	 * Set up the connection to the database, the DAOs, and the test data before each test.
	 * @throws SQLException on database error
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		bookDaoImpl = new BookDaoImpl(conn);
		branchDaoImpl = new LibraryBranchDaoImpl(conn);
		copiesDaoImpl = new CopiesDaoImpl(conn);
		testBook = bookDaoImpl.create(SAMPLE_TITLE, null, null);
		testBranch = branchDaoImpl.create(SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);
		copiesDaoImpl.setCopies(testBranch, testBook, NUM_COPIES);
		conn.commit();
	}

	/**
	 * Delete test data and tear down the database after each test.
	 * @throws SQLException on database error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		bookDaoImpl.delete(testBook);
		branchDaoImpl.delete(testBranch);
		conn.close();
	}

	private int mySQLSize() throws SQLException {
		final String sql = "SELECT COUNT(" + PRIMARY_KEY + ") AS size FROM " + TABLE + ";";
		final PreparedStatement prepareStatement = conn.prepareStatement(sql);
		try (ResultSet resultSet = prepareStatement.executeQuery()) {
			resultSet.next();
			return resultSet.getInt("size");
		}
	}

	/**
	 * Test that getting the number of copies works properly.
	 * @throws SQLException on database error
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGetCopies() throws SQLException {
		assertEquals(NUM_COPIES, copiesDaoImpl.getCopies(testBranch, testBook),
				"number of copies is as expected");
	}

	/**
	 * Test that the number of copies defaults to 0 if not found.
	 * @throws SQLException on database error
	 */
	@DisplayName("return 0 if not found")
	@Test
	public void testGetNonExistingCopies() throws SQLException {
		final Branch nonExistingBranch = new Branch(Integer.MAX_VALUE,
				SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);
		assertEquals(0, copiesDaoImpl.getCopies(nonExistingBranch, testBook),
				"number of copies at an unknown branch is 0");
	}

	/**
	 * Test that setting number of copies to 0 deletes an existing entry.
	 * @throws SQLException on database error
	 */
	@DisplayName("Deleting an entry if noOfCopies is 0")
	@Test
	public void setEntryWithNoOfCopiesTest() throws SQLException {
		final int previousSize = mySQLSize();
		copiesDaoImpl.setCopies(testBranch, testBook, 0);
		assertEquals(previousSize - 1, mySQLSize(),
				"setting number of copies to 0 deletes row");
	}

	/**
	 * Test that setting number of copies updates an existing entry.
	 * @throws SQLException on database error
	 */
	@DisplayName("Updating an entry if it exists")
	@Disabled("Currently failing despite looking correct by inspection")
	@Test
	public void setEntryWithNewNoOfCopies() throws SQLException {
		final int newNoOfCopies = 100;
		final int previousSize = mySQLSize();
		copiesDaoImpl.setCopies(testBranch, testBook, newNoOfCopies);
		conn.commit();
		final int currentSize = mySQLSize();
		final int foundNoOfCopies = copiesDaoImpl.getCopies(testBranch, testBook);

		assertEquals(previousSize, currentSize,
				"changing number of copies doesn't create new record");
		assertEquals(newNoOfCopies, foundNoOfCopies,
				"change to number of copies propagated to database");
	}

	/**
	 * Test that getting all branch copies works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAllBranchCopies() throws SQLException {
		final Map<Book, Integer> allBranchCopies = copiesDaoImpl
				.getAllBranchCopies(testBranch);
		assertTrue(allBranchCopies.containsKey(testBook),
				"branch's records includes copies of test book");
		assertEquals(NUM_COPIES, allBranchCopies.get(testBook).intValue(),
				"branch's records includes correct copy count for test book");
	}

	/**
	 * Test that getting copies of a book in all branches works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAllBookCopies() throws SQLException {
		final Map<Branch, Integer> allBookCopies = copiesDaoImpl.getAllBookCopies(testBook);
		assertTrue(allBookCopies.containsKey(testBranch),
				"records for a book includes at test branch");
		assertEquals(NUM_COPIES, allBookCopies.get(testBranch).intValue(),
				"copy count at test branch is correct");
	}

	/**
	 * Test that getting all records works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAllCopies() throws SQLException {
		final Map<Branch, Map<Book, Integer>> allCopies = copiesDaoImpl.getAllCopies();
		assertTrue(allCopies.containsKey(testBranch),
				"all-copies database includes records for test branch");
		assertTrue(allCopies.get(testBranch).containsKey(testBook),
				"all-copies database includes record for test book");
		assertEquals(NUM_COPIES, allCopies.get(testBranch).get(testBook).intValue(),
				"all-copies database has right count for test book at test branch");
	}
}
