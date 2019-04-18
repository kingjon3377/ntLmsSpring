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

import com.st.novatech.springlms.model.Branch;

/**
 * Tests of the library-branch DAO.
 * @author Salem Ozaki
 * @author Jonathan Lovelace (integration and polishing)
 */
public class SalemLibraryBranchDaoTest {
	/**
	 * Sample branch name for tests.
	 */
	private static final String SAMPLE_BRANCH_NAME = "The Branch";
	/**
	 * Sample branch address for tests.
	 */
	private static final String SAMPLE_BRANCH_ADDRESS = "601 New Jersey Ave, Washington, DC 20001";

	/**
	 * Branch DAO under test.
	 */
	private LibraryBranchDao branchDaoImpl;
	/**
	 * Stored branch from tests.
	 *
	 * <p>(TODO: Is this ever read without being first written to in the same test?)
	 */
	private Branch testBranch;

	/**
	 * Database connection.
	 */
	private Connection conn;

	/**
	 * Database table this DAO represents.
	 */
	private static final String TABLE = "tbl_library_branch";
	/**
	 * Primary key field in the database table.
	 */
	private static final String PRIMARY_KEY = "branchId";

	/**
	 * Set up database connection, DAO, and test data before each test.
	 * @throws SQLException on database error
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void init() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		branchDaoImpl = new LibraryBranchDaoImpl(conn);
		testBranch = branchDaoImpl.create(SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);
	}

	/**
	 * Delete test data and tear down the database connection after each test.
	 * @throws SQLException on database error
	 */
	@AfterEach
	public void tearThis() throws SQLException {
		// FIXME?: WARNING maybe something that doesn't call the method we are trying to test
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
	 * Test that creating a branch works.
	 * @throws SQLException on database error
	 */
	@Test
	public void createBranchTest() throws SQLException {
		branchDaoImpl.delete(testBranch);

		final int previousSize = mySQLSize();

		testBranch = branchDaoImpl.create(SAMPLE_BRANCH_NAME, SAMPLE_BRANCH_ADDRESS);

		final int currentSize = mySQLSize();

		assertTrue(previousSize < currentSize, "creating a branch creates a row");
		assertEquals(SAMPLE_BRANCH_NAME, testBranch.getName(),
				"new branch has expected name");
		assertEquals(SAMPLE_BRANCH_ADDRESS, testBranch.getAddress(),
				"new branch has expected address");
	}

	/**
	 * Test that deleting a branch works.
	 * @throws SQLException on database error
	 */
	@Test
	public void deleteBranchTest() throws SQLException {
		final int previousSize = mySQLSize();

		branchDaoImpl.delete(testBranch);

		final int currentSize = mySQLSize();

		assertTrue(previousSize > currentSize, "deleting a branch removes a row");
		assertNull(branchDaoImpl.get(testBranch.getId()),
				"deleted branch is gone from database");
	}

	/**
	 * Test that updating a branch works.
	 * @throws SQLException on database error
	 */
	@DisplayName("Update Branch Correctly")
	@Test
	public void updateBranchTest() throws SQLException {
		final String newBranchName = "Branch Person";
		final String newBranchAddress = "123 new address in VA";
		final Branch newBranch = new Branch(testBranch.getId(), newBranchName,
				newBranchAddress);

		branchDaoImpl.update(newBranch);

		final Branch updatedBranch = branchDaoImpl.get(newBranch.getId());

		assertNotNull(updatedBranch, "updated row is still present");
		assertEquals(updatedBranch, newBranch, "updated row has expected fields");
	}

	/**
	 * Test that retrieving a branch by ID works.
	 * @throws SQLException on database error
	 */
	@DisplayName("Get correctly")
	@Test
	public void testGetBranch() throws SQLException {
		final Branch foundBranch = branchDaoImpl.get(testBranch.getId());
		assertNotNull(foundBranch, "retrieved branch was not null");
		assertEquals(testBranch, foundBranch, "expected branch was retrieved");
	}

	/**
	 * Test that null is retrieved for unknown ID.
	 * @throws SQLException on database error
	 */
	@DisplayName("Return null if entry not found")
	@Test
	public void testGetNotFoundBranch() throws SQLException {
		assertNull(branchDaoImpl.get(Integer.MAX_VALUE),
				"null is retrieved for unknown ID.");
	}

	/**
	 * Test that retrieving all branches works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAll() throws SQLException {
		final List<Branch> listOfBranches = branchDaoImpl.getAll();
		assertEquals(listOfBranches.size(), mySQLSize(),
				"DAO and SQL agree on number of branches");
	}

}
