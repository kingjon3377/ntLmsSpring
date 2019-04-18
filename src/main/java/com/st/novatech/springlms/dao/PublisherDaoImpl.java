package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.st.novatech.springlms.model.Publisher;

/**
 * A DAO to provide an interface between the "publisher" table in the database
 * and the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class PublisherDaoImpl implements PublisherDao {
	/**
	 * The SQL query to update existing publisher rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete publisher rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find a publisher by its ID.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all publishers from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new publisher into the database.
	 */
	private final PreparedStatement createStatement;
	/**
	 * The SQL query to get the newly created publisher's ID from the database.
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
	public PublisherDaoImpl(final Connection dbConnection) throws SQLException {
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_publisher` SET `publisherName` = ?, `publisherAddress` = ?, `publisherPhone` = ? WHERE `publisherId` = ?");
		deleteStatement = dbConnection.prepareStatement(
				"DELETE FROM `tbl_publisher` WHERE `publisherId` = ?");
		findStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_publisher` WHERE `publisherId` = ?");
		getAllStatement = dbConnection
				.prepareStatement("SELECT * FROM `tbl_publisher`");
		createStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_publisher` (`publisherName`, `publisherAddress`, `publisherPhone`) VALUES (?, ?, ?)");
		findCreatedStatement = dbConnection.prepareStatement(
				"SELECT `publisherId` FROM `tbl_publisher` WHERE `publisherName` = ? ORDER BY `publisherId` DESC LIMIT 1");
	}

	@Override
	public void update(final Publisher publisher) throws SQLException {
		synchronized (updateStatement) {
			updateStatement.setString(1, publisher.getName());
			if (publisher.getAddress().isEmpty()) {
				updateStatement.setNull(2, Types.VARCHAR);
			} else {
				updateStatement.setString(2, publisher.getAddress());
			}
			if (publisher.getPhone().isEmpty()) {
				updateStatement.setNull(3, Types.VARCHAR);
			} else {
				updateStatement.setString(3, publisher.getPhone());
			}
			updateStatement.setInt(4, publisher.getId());
			updateStatement.executeUpdate();
		}
	}

	@Override
	public void delete(final Publisher publisher) throws SQLException {
		if (publisher != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, publisher.getId());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Publisher get(final int id) throws SQLException {
		synchronized (findStatement) {
			findStatement.setInt(1, id);
			try (ResultSet result = findStatement.executeQuery()) {
				Publisher retval = null;
				while (result.next()) {
					if (retval == null) {
						retval = new Publisher(id, result.getString("publisherName"),
								Optional.ofNullable(
										result.getString("publisherAddress"))
										.orElse(""),
								Optional.ofNullable(
										result.getString("publisherPhone"))
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
	public List<Publisher> getAll() throws SQLException {
		final List<Publisher> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					retval.add(new Publisher(result.getInt("publisherId"),
							result.getString("publisherName"),
							Optional.ofNullable(result.getString("publisherAddress"))
									.orElse(""),
							Optional.ofNullable(result.getString("publisherPhone"))
									.orElse("")));
				}
				return retval;
			}
		}
	}

	@Override
	public Publisher create(final String publisherName,
			final String publisherAddress, final String publisherPhone)
			throws SQLException {
		synchronized (createStatement) {
			createStatement.setString(1, publisherName);
			findCreatedStatement.setString(1, publisherName);
			if (publisherAddress.isEmpty()) {
				createStatement.setNull(2, Types.VARCHAR);
			} else {
				createStatement.setString(2, publisherAddress);
			}
			if (publisherPhone.isEmpty()) {
				createStatement.setNull(3, Types.VARCHAR);
			} else {
				createStatement.setString(3, publisherPhone);
			}
			createStatement.executeUpdate();
			try (ResultSet result = findCreatedStatement.executeQuery()) {
				result.next();
				return new Publisher(result.getInt("publisherId"), publisherName,
						publisherAddress, publisherPhone);
			}
		}
	}
}
