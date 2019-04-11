package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.st.novatech.springlms.model.Branch;

/**
 * An implementation of the branch DAO backed by a list.
 * @author Jonathan Lovelace
 *
 */
public final class InMemoryLibraryBranchDao implements LibraryBranchDao {
	/**
	 * The backing list.
	 */
	private final List<Branch> backing = new ArrayList<>();
	/**
	 * The highest ID we've given out.
	 */
	private int highestId = 0;

	@Override
	public void update(final Branch branch) throws SQLException {
		final ListIterator<Branch> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == branch.getId()) {
				iterator.set(branch);
			}
		}
	}

	@Override
	public void delete(final Branch branch) throws SQLException {
		final ListIterator<Branch> iterator = backing.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next().getId() == branch.getId()) {
				iterator.remove();
			}
		}
	}

	@Override
	public Branch get(final int id) throws SQLException {
		return backing.stream().filter(branch -> branch.getId() == id).findAny()
				.orElse(null);
	}

	@Override
	public List<Branch> getAll() throws SQLException {
		return new ArrayList<>(backing);
	}

	@Override
	public synchronized Branch create(final String branchName, final String branchAddress)
			throws SQLException {
		final int id = highestId + 1;
		highestId = id;
		final Branch retval = new Branch(id, branchName, branchAddress);
		backing.add(retval);
		return retval;
	}

}
