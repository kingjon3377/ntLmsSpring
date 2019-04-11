package com.st.novatech.springlms.dao;

import java.sql.SQLException;

import com.st.novatech.springlms.model.Borrower;

/**
 * A Data Access Object interface to access the table of borrowers.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface BorrowerDao extends Dao<Borrower> {
	/**
	 * Create a borrower object and add it to the database.
	 *
	 * @param borrowerName    the name of the borrower
	 * @param borrowerAddress the borrower's address
	 * @param borrowerPhone   the borrower's phone number
	 * @return the newly created borrower object
	 * @throws SQLException on unexpected error in dealing with the database
	 */
	Borrower create(String borrowerName, String borrowerAddress, String borrowerPhone) throws SQLException;
}
