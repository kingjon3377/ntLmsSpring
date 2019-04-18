package com.st.novatech.springlms.dao;

import java.sql.SQLException;

import com.st.novatech.springlms.model.Branch;

/**
 * A Data Access Object class to access the table of library branches.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface LibraryBranchDao extends Dao<Branch> {
	/**
	 * Create a library-branch object and add it to the database.
	 *
	 * @param branchName the name of the branch
	 * @param branchAddress the address of the branch
	 * @return the newly created branch object
	 * @throws SQLException on unexpected error in dealing with the database
	 */
	Branch create(String branchName, String branchAddress) throws SQLException;
}
