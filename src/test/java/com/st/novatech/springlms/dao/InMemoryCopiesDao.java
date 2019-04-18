package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;

/**
 * An implementation of the copies DAO backed by a map.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryCopiesDao implements CopiesDao {
	/**
	 * The backing map.
	 */
	private final Map<Branch, Map<Book, Integer>> backing = new HashMap<>();

	@Override
	public int getCopies(final Branch branch, final Book book) throws SQLException {
		return Optional.ofNullable(backing.get(branch))
				.flatMap(map -> Optional.ofNullable(map.get(book))).orElse(0);
	}

	@Override
	public synchronized void setCopies(final Branch branch, final Book book,
			final int noOfCopies) throws SQLException {
		Map<Book, Integer> inner;
		if (backing.containsKey(branch)) {
			inner = backing.get(branch);
		} else {
			inner = new HashMap<>();
			backing.put(branch, inner);
		}
		inner.put(book, noOfCopies);
	}

	@Override
	public Map<Book, Integer> getAllBranchCopies(final Branch branch)
			throws SQLException {
		return new HashMap<>(
				Optional.ofNullable(backing.get(branch)).orElseGet(HashMap::new));
	}

	@Override
	public Map<Branch, Integer> getAllBookCopies(final Book book)
			throws SQLException {
		final Map<Branch, Integer> retval = new HashMap<>();
		for (final Entry<Branch, Map<Book, Integer>> entry : backing.entrySet()) {
			if (entry.getValue().containsKey(book)) {
				retval.put(entry.getKey(), entry.getValue().get(book));
			}
		}
		return retval;
	}

	@Override
	public Map<Branch, Map<Book, Integer>> getAllCopies() throws SQLException {
		final Map<Branch, Map<Book, Integer>> retval = new HashMap<>();
		backing.forEach((branch, inner) -> retval.put(branch, new HashMap<>(inner)));
		return retval;
	}
}
