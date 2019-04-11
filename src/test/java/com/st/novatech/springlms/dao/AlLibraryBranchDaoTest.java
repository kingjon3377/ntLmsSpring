package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Branch;

/**
 * Tests of branch DAO.
 *
 * @author Al Amine Ahmed Moussa
 * @author Jonathan Lovelace (integration and polishing)
 */
public class AlLibraryBranchDaoTest {
	/**
	 * The DAO under test.
	 */
	private LibraryBranchDao libraryBranchDao;
	/**
	 * The connection to the database.
	 */
	private Connection conn;

	/**
	 * Set up the DB connection and the DAO before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void setUp() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		libraryBranchDao = new LibraryBranchDaoImpl(conn);
	}

	/**
	 * Tear down the database after each test.
	 *
	 * @throws SQLException on database error while closing the connection
	 */
	@AfterEach
	public void tearDown() throws SQLException {
		conn.close();
	}

	/**
	 * Test that creating a branch works.
	 * @throws SQLException on database error
	 */
	@Test
	public void createTest() throws SQLException {
		final String str1 = "Branch1";
		final String str2 = "AddressTest1";

		final Branch branch = libraryBranchDao.create(str1, str2);
		assertEquals(str1, branch.getName(), "created branch has expected name");
		assertEquals(str2, branch.getAddress(), "created branch has expected address");
	}

	/**
	 * Test that getting a branch works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGet() throws SQLException {
		final Branch p = libraryBranchDao.create("Branch 1457", "ADR45");
		assertEquals(p.getName(), libraryBranchDao.get(p.getId()).getName(),
				"retrieved branch has expected name");
	}
}
