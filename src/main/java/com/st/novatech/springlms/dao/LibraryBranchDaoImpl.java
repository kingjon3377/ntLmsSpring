package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.st.novatech.springlms.model.Branch;

/**
 * A DAO to provide an interface between the "library branch" table in the
 * database and the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class LibraryBranchDaoImpl implements LibraryBranchDao {
	/**
	 * The SQL query to update existing branch rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete branch rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find a branch by its ID.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all branches from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new branch into the database.
	 */
	private final PreparedStatement createStatement;
	/**
	 * The SQL query to get the newly created branch's ID from the database.
	 */
	private final PreparedStatement findCreatedStatement;

	/**
	 * To construct an instance of a DAO, the caller must provide a connection to
	 * the database.
	 *
	 * @param dbConnection the database connection
	 * @throws SQLException on any unexpected database condition while setting up
	 *                      prepared statements
	 */
	public LibraryBranchDaoImpl(final Connection dbConnection) throws SQLException {
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_library_branch` SET `branchName` = ?, `branchAddress` = ? WHERE `branchId` = ?");
		deleteStatement = dbConnection.prepareStatement(
				"DELETE FROM `tbl_library_branch` WHERE `branchId` = ?");
		findStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_library_branch` WHERE `branchId` = ?");
		getAllStatement = dbConnection
				.prepareStatement("SELECT * FROM `tbl_library_branch`");
		createStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_library_branch` (`branchName`, `branchAddress`) VALUES (?, ?)");
		findCreatedStatement = dbConnection.prepareStatement(
				"SELECT `branchId` FROM `tbl_library_branch` ORDER BY `branchId` DESC LIMIT 1");
	}

	@Override
	public void update(final Branch branch) throws SQLException {
		synchronized (updateStatement) {
			if (branch.getName().isEmpty()) {
				updateStatement.setNull(1, Types.VARCHAR);
			} else {
				updateStatement.setString(1, branch.getName());
			}
			if (branch.getAddress().isEmpty()) {
				updateStatement.setNull(2, Types.VARCHAR);
			} else {
				updateStatement.setString(2, branch.getAddress());
			}
			updateStatement.setInt(3, branch.getId());
			updateStatement.executeUpdate();
		}
	}

	@Override
	public void delete(final Branch branch) throws SQLException {
		if (branch != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, branch.getId());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Branch get(final int id) throws SQLException {
		synchronized (findStatement) {
			findStatement.setInt(1, id);
			try (ResultSet result = findStatement.executeQuery()) {
				Branch retval = null;
				while (result.next()) {
					if (retval == null) {
						retval = new Branch(id,
								Optional.ofNullable(result.getString("branchName"))
										.orElse(""),
								Optional.ofNullable(
										result.getString("branchAddress"))
										.orElse(""));
					} else {
						throw new IllegalStateException("Multiple results for key");
					}
				}
				return retval;
			}
		}
	}

	@Override
	public List<Branch> getAll() throws SQLException {
		final List<Branch> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					retval.add(new Branch(result.getInt("branchId"),
							Optional.ofNullable(result.getString("branchName"))
									.orElse(""),
							Optional.ofNullable(result.getString("branchAddress"))
									.orElse("")));
				}
				return retval;
			}
		}
	}

	@Override
	public Branch create(final String branchName, final String branchAddress)
			throws SQLException {
		synchronized (createStatement) {
			if (branchName.isEmpty()) {
				createStatement.setNull(1, Types.VARCHAR);
			} else {
				createStatement.setString(1, branchName);
			}
			if (branchAddress.isEmpty()) {
				createStatement.setNull(2, Types.VARCHAR);
			} else {
				createStatement.setString(2, branchAddress);
			}
			createStatement.executeUpdate();
			try (ResultSet result = findCreatedStatement.executeQuery()) {
				result.next();
				return new Branch(result.getInt("branchId"), branchName,
						branchAddress);
			}
		}
	}
}
