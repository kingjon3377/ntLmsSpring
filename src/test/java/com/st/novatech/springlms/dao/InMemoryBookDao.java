package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;

/**
 * An implementation of the book DAO backed by a list.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryBookDao implements BookDao {
	/**
	 * The backing list.
	 */
	private final List<Book> backing = new ArrayList<>();
	/**
	 * The highest ID we've given out.
	 */
	private int highestId = 0;

	@Override
	public void update(final Book book) throws SQLException {
		final ListIterator<Book> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == book.getId()) {
				iterator.set(book);
			}
		}
	}

	@Override
	public void delete(final Book author) throws SQLException {
		final ListIterator<Book> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == author.getId()) {
				iterator.remove();
			}
		}
	}

	@Override
	public Book get(final int id) throws SQLException {
		return backing.stream().filter(author -> author.getId() == id).findAny()
				.orElse(null);
	}

	@Override
	public List<Book> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public synchronized Book create(final String title, final Author author,
			final Publisher publisher) throws SQLException {
		final int id = highestId + 1;
		highestId = id;
		final Book retval = new Book(id, title, author, publisher);
		backing.add(retval);
		return retval;
	}

}
