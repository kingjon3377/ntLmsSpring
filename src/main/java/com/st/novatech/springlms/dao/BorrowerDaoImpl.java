package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.st.novatech.springlms.model.Borrower;

/**
 * A DAO to provide an interface between the "borrowers" table in the database and
 * the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class BorrowerDaoImpl implements BorrowerDao {
	/**
	 * The SQL query to update existing borrower rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete borrower rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find a borrower by his or her ID.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all borrowers from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new borrower into the database.
	 */
	private final PreparedStatement createStatement;
	/**
	 * The SQL query to get the newly created borrower's ID from the database.
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
	public BorrowerDaoImpl(final Connection dbConnection) throws SQLException {
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_borrower` SET `name` = ?, `address` = ?, `phone` = ? WHERE `cardNo` = ?");
		deleteStatement = dbConnection
				.prepareStatement("DELETE FROM `tbl_borrower` WHERE `cardNo` = ?");
		findStatement = dbConnection
				.prepareStatement("SELECT * FROM `tbl_borrower` WHERE `cardNo` = ?");
		getAllStatement = dbConnection.prepareStatement("SELECT * FROM `tbl_borrower`");
		createStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_borrower` (`name`, `address`, `phone`) VALUES (?, ?, ?)");
		findCreatedStatement = dbConnection.prepareStatement(
				"SELECT `cardNo` FROM `tbl_borrower` WHERE `name` = ? ORDER BY `cardNo` DESC LIMIT 1");
	}

	@Override
	public void update(final Borrower borrower) throws SQLException {
		synchronized (updateStatement) {
			updateStatement.setString(1, borrower.getName());
			if (borrower.getAddress().isEmpty()) {
				updateStatement.setNull(2, Types.VARCHAR);
			} else {
				updateStatement.setString(2, borrower.getAddress());
			}
			if (borrower.getPhone().isEmpty()) {
				updateStatement.setNull(3, Types.VARCHAR);
			} else {
				updateStatement.setString(3, borrower.getPhone());
			}
			updateStatement.setInt(4, borrower.getCardNo());
			updateStatement.executeUpdate();
		}
	}

	@Override
	public void delete(final Borrower borrower) throws SQLException {
		if (borrower != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, borrower.getCardNo());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Borrower get(final int id) throws SQLException {
		synchronized (findStatement) {
			findStatement.setInt(1, id);
			try (ResultSet result = findStatement.executeQuery()) {
				Borrower retval = null;
				while (result.next()) {
					if (retval == null) {
						retval = new Borrower(id,
								Optional.ofNullable(result.getString("name"))
										.orElse(""),
								Optional.ofNullable(result.getString("address"))
										.orElse(""),
								Optional.ofNullable(result.getString("phone"))
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
	public List<Borrower> getAll() throws SQLException {
		final List<Borrower> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					retval.add(new Borrower(result.getInt("cardNo"),
							Optional.ofNullable(result.getString("name")).orElse(""),
							Optional.ofNullable(result.getString("address"))
									.orElse(""),
							Optional.ofNullable(result.getString("phone"))
									.orElse("")));
				}
			}
		}
		return retval;
	}

	@Override
	public Borrower create(final String borrowerName, final String borrowerAddress,
			final String borrowerPhone) throws SQLException {
		synchronized (createStatement) {
			createStatement.setString(1, borrowerName);
			findCreatedStatement.setString(1, borrowerName);
			if (borrowerAddress.isEmpty()) {
				createStatement.setNull(2, Types.VARCHAR);
			} else {
				createStatement.setString(2, borrowerAddress);
			}
			if (borrowerPhone.isEmpty()) {
				createStatement.setNull(3, Types.VARCHAR);
			} else {
				createStatement.setString(3, borrowerPhone);
			}
			createStatement.executeUpdate();
			try (ResultSet result = findCreatedStatement.executeQuery()) {
				result.next();
				return new Borrower(result.getInt("cardNo"), borrowerName,
						borrowerAddress, borrowerPhone);
			}
		}
	}
}
