package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.st.novatech.springlms.model.Borrower;

/**
 * An implementation of the borrower DAO backed by a list.
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryBorrowerDao implements BorrowerDao {
	/**
	 * The backing list.
	 */
	private final List<Borrower> backing = new ArrayList<>();
	/**
	 * The highest ID we've given out.
	 */
	private int highestId = 0;

	@Override
	public void update(final Borrower borrower) throws SQLException {
		final ListIterator<Borrower> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getCardNo() == borrower.getCardNo()) {
				iterator.set(borrower);
			}
		}
	}

	@Override
	public void delete(final Borrower borrower) throws SQLException {
		final ListIterator<Borrower> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getCardNo() == borrower.getCardNo()) {
				iterator.remove();
			}
		}
	}

	@Override
	public Borrower get(final int id) throws SQLException {
		return backing.stream().filter(borrower -> borrower.getCardNo() == id).findAny()
				.orElse(null);
	}

	@Override
	public List<Borrower> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public synchronized Borrower create(final String borrowerName, final String borrowerAddress,
			final String borrowerPhone) throws SQLException {
		final int id = highestId + 1;
		highestId = id;
		final Borrower retval = new Borrower(id, borrowerName, borrowerAddress, borrowerPhone);
		backing.add(retval);
		return retval;
	}

}
