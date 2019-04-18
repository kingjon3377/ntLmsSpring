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

import com.st.novatech.springlms.model.Borrower;

/**
 * Tests of the borrower DAO.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemBorrowerDaoTest {
	/**
	 * Sample borrower name for tests.
	 */
	private static final String SAMPLE_PATRON_NAME = "Jack Blaze";
	/**
	 * Sample borrower address for tests.
	 */
	private static final String SAMPLE_PATRON_ADDRESS = "601 New Jersey Ave, Washington, DC 20001";
	/**
	 * Sample borrower phone for tests.
	 */
	private static final String SAMPLE_PATRON_PHONE = "1234567890";

	/**
	 * Stored borrower from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Borrower testBorrower;
	/**
	 * Borrower DAO under test.
	 */
	private BorrowerDao borrowerDaoImpl;

	/**
	 * Database connection.
	 */
	private Connection conn;
	/**
	 * The table this DAO represents.
	 */
	private static final String TABLE = "tbl_borrower";
	/**
	 * The primary key field in the table.
	 */
	private static final String KEY_FIELD = "cardNo";

	/**
	 * Set up the database connection, the DAO, and the test data before each test.
	 *
	 * @throws SQLException on database error.
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		borrowerDaoImpl = new BorrowerDaoImpl(conn);
		testBorrower = borrowerDaoImpl.create(SAMPLE_PATRON_NAME,
				SAMPLE_PATRON_ADDRESS, SAMPLE_PATRON_PHONE);
	}

	/**
	 * Delete test data and tear down the database after each test.
	 *
	 * @throws SQLException on database error.
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		borrowerDaoImpl.delete(testBorrower);
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
	 * Test that creating a borrower works.
	 *
	 * @throws SQLException on database error
	 */
	@Test
	public void createBorrowerTest() throws SQLException {
		borrowerDaoImpl.delete(testBorrower);

		final int previousSize = mySQLSize();

		testBorrower = borrowerDaoImpl.create(SAMPLE_PATRON_NAME,
				SAMPLE_PATRON_ADDRESS, SAMPLE_PATRON_PHONE);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize, "creating borrower adds a row");
		assertEquals(SAMPLE_PATRON_NAME, testBorrower.getName(),
				"created borrower has expected name");
		assertEquals(SAMPLE_PATRON_ADDRESS, testBorrower.getAddress(),
				"created borrower has expected address");
		assertEquals(SAMPLE_PATRON_PHONE, testBorrower.getPhone(),
				"created borrower has expected phone");
	}

	/**
	 * Test that deleting a borrower works.
	 *
	 * @throws SQLException on database error.
	 */
	@Test
	public void deleteBorrowerTest() throws SQLException {
		final int previousSize = mySQLSize();

		borrowerDaoImpl.delete(testBorrower);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize, "deleting borrower deletes row");
		assertNull(borrowerDaoImpl.get(testBorrower.getCardNo()),
				"borrower is gone after deletion");
	}

	/**
	 * Test that updating a borrower works.
	 *
	 * @throws SQLException on database error.
	 */
	@DisplayName("Update Correctly")
	@Test
	public void updateBorrowerTest() throws SQLException {
		final String newBorrowerName = "New Borrower Name";
		final String newBorrowerAddress = "New Address";
		final String newBorrowerPhone = "4567891230";

		final Borrower newBorrower = new Borrower(testBorrower.getCardNo(),
				newBorrowerName, newBorrowerAddress, newBorrowerPhone);

		borrowerDaoImpl.update(newBorrower);

		final Borrower updatedborrower = borrowerDaoImpl
				.get(newBorrower.getCardNo());

		assertNotNull(updatedborrower, "updated row is still there");
		assertEquals(newBorrower, updatedborrower, "updated row has expected data");
	}

	/**
	 * Test that retrieval works.
	 *
	 * @throws SQLException on database error
	 */
	@DisplayName("Get Borrower correctly")
	@Test
	public void testGetBorrower() throws SQLException {
		final Borrower foundBorrower = borrowerDaoImpl.get(testBorrower.getCardNo());
		assertNotNull(foundBorrower, "retrieved row was present");
		assertEquals(testBorrower, foundBorrower, "retrieved row has expected data");
	}

	/**
	 * Test that retrieval returns null if ID not present.
	 *
	 * @throws SQLException on database error
	 */
	@DisplayName("Return null if entry not found")
	@Test
	public void testGetNotFoundBorrower() throws SQLException {
		final Borrower foundBorrower = borrowerDaoImpl.get(Integer.MAX_VALUE);
		assertNull(foundBorrower, "row was not found");
	}

	/**
	 * Test that retrieving the whole table works.
	 *
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Borrower> listOfBorrowers = borrowerDaoImpl.getAll();
		final int borrowerSize = mySQLSize();
		assertEquals(listOfBorrowers.size(), borrowerSize,
				"DAO and SQL agree on number of borrowers");
	}
}
