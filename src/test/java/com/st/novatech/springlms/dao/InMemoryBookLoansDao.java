package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;

/**
 * An implementation of the book-loans DAO backed by a list.
 *
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryBookLoansDao implements BookLoansDao {
	/**
	 * The backing list.
	 */
	private final List<Loan> backing = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void update(final Loan loan) throws SQLException {
		final ListIterator<Loan> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			final Loan next = iterator.next();
			if (Objects.equals(loan.getBook(), next.getBook())
					&& Objects.equals(loan.getBorrower(), next.getBorrower())
					&& Objects.equals(loan.getBranch(), next.getBranch())) {
				iterator.set(loan);
			}
		}
	}

	@Override
	public void delete(final Loan loan) throws SQLException {
		final ListIterator<Loan> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			final Loan next = iterator.next();
			if (Objects.equals(loan.getBook(), next.getBook())
					&& Objects.equals(loan.getBorrower(), next.getBorrower())
					&& Objects.equals(loan.getBranch(), next.getBranch())) {
				iterator.remove();
			}
		}
	}

	@Override
	public List<Loan> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public Loan create(final Book book, final Borrower borrower, final Branch branch,
			final LocalDateTime dateOut, final LocalDate dueDate)
			throws SQLException {
		final Loan retval = new Loan(book, borrower, branch, dateOut, dueDate);
		backing.add(retval);
		return retval;
	}

	@Override
	public Loan get(final Book book, final Borrower borrower, final Branch branch)
			throws SQLException {
		return backing.parallelStream()
				.filter(loan -> Objects.equals(loan.getBook(), book)
						&& Objects.equals(loan.getBorrower(), borrower)
						&& Objects.equals(loan.getBranch(), branch))
				.findAny().orElse(null);
	}
}
