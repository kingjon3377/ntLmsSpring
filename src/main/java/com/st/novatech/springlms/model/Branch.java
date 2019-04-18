package com.st.novatech.springlms.model;

import java.util.Objects;

/**
 * A branch of a library.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public class Branch {
	/**
	 * The ID number used to identify this branch in the database.
	 */
	private final int id;
	/**
	 * The name of the branch.
	 */
	private String name;
	/**
	 * The address of the branch.
	 */
	private String address;

	/**
	 * To construct a branch object, callers must supply its ID number, name, and
	 * address.
	 *
	 * @param id      the ID number to identify this branch.
	 * @param name    The name of the branch
	 * @param address The address of the branch
	 */
	public Branch(final int id, final String name, final String address) {
		this.id = id;
		this.name = name;
		this.address = address;
	}

	/**
	 * Get the name of the branch, which will not be null.
	 * @return the name of the branch
	 */
	public String getName() {
		return name;
	}

	/**
	 * Change the name of the branch.
	 * @param name The new name of the branch, which must not be null.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the address of the branch as a string, which will not be null.
	 * @return the address of the branch
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Change the address of the branch.
	 * @param address the branch's new address, which must not be null.
	 */
	public void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * The ID number that identifies this branch in the database.
	 * @return this branch's ID number.
	 */
	public int getId() {
		return id;
	}

	/**
	 * We use only the ID for this object's hash-code.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * An object is equal to this one iff it is a Branch with equal ID, name, and
	 * address.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Branch) {
			return id == ((Branch) obj).getId()
					&& Objects.equals(name, ((Branch) obj).getName())
					&& Objects.equals(address, ((Branch) obj).getAddress());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Branch: " + name + "(" + id + ") at " + address;
	}
}
