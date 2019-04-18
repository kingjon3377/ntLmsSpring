package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.st.novatech.springlms.model.Author;

/**
 * An implementation of the author DAO backed by a list.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryAuthorDao implements AuthorDao {
	/**
	 * The backing list.
	 */
	private final List<Author> backing = new ArrayList<>();
	/**
	 * The highest ID we've given out.
	 */
	private int highestId = 0;

	@Override
	public void update(final Author author) throws SQLException {
		final ListIterator<Author> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == author.getId()) {
				iterator.set(author);
			}
		}
	}

	@Override
	public void delete(final Author author) throws SQLException {
		final ListIterator<Author> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == author.getId()) {
				iterator.remove();
			}
		}
	}

	@Override
	public Author get(final int id) throws SQLException {
		return backing.stream().filter(author -> author.getId() == id).findAny()
				.orElse(null);
	}

	@Override
	public List<Author> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public synchronized Author create(final String authorName) throws SQLException {
		final int id = highestId + 1;
		highestId = id;
		final Author retval = new Author(id, authorName);
		backing.add(retval);
		return retval;
	}

}
