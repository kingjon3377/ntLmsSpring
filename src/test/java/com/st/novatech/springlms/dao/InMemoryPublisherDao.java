package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.st.novatech.springlms.model.Publisher;

/**
 * An implementation of the publisher DAO backed by a list.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryPublisherDao implements PublisherDao {
	/**
	 * The backing list.
	 */
	private final List<Publisher> backing = new ArrayList<>();
	/**
	 * The highest ID we've given out.
	 */
	private int highestId = 0;

	@Override
	public void update(final Publisher publisher) throws SQLException {
		final ListIterator<Publisher> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == publisher.getId()) {
				iterator.set(publisher);
			}
		}
	}

	@Override
	public void delete(final Publisher author) throws SQLException {
		final ListIterator<Publisher> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == author.getId()) {
				iterator.remove();
			}
		}
	}

	@Override
	public Publisher get(final int id) throws SQLException {
		return backing.stream().filter(publisher -> publisher.getId() == id)
				.findAny().orElse(null);
	}

	@Override
	public List<Publisher> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public synchronized Publisher create(final String publisherName,
			final String publisherAddress, final String publisherPhone) throws SQLException {
		final int id = highestId + 1;
		highestId = id;
		final Publisher retval = new Publisher(id, publisherName, publisherAddress,
				publisherPhone);
		backing.add(retval);
		return retval;
	}

}
