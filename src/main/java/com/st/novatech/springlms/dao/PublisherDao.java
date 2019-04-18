package com.st.novatech.springlms.dao;

import java.sql.SQLException;

import com.st.novatech.springlms.model.Publisher;

/**
 * A Data Access Object class to access the table of publishers.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface PublisherDao extends Dao<Publisher> {
	/**
	 * Create a publisher object and add the publisher to the database.
	 * @param publisherName the name of the publisher
	 * @param publisherAddress the publisher's address
	 * @param publisherPhone the publisher's phone number
	 * @return the newly created publisher object
	 * @throws SQLException on unexpected error in dealing with the database
	 */
	Publisher create(String publisherName, String publisherAddress, String publisherPhone) throws SQLException;
}
