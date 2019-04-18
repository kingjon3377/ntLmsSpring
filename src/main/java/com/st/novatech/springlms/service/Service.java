package com.st.novatech.springlms.service;

import java.util.List;

import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Branch;

/**
 * A base interface that all service interfaces extend.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface Service {
	/**
	 * Get a list (order should not be relied on) of all the library branches in the
	 * database.
	 *
	 * @return all the borrowers in the database.
	 */
	List<Branch> getAllBranches() throws TransactionException;
	/**
	 * Commit all outstanding operations to the database, if the backend supports transactions.
	 */
	void commit() throws TransactionException;
}
