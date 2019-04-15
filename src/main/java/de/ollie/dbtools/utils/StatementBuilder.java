package de.ollie.dbtools.utils;

import java.util.Objects;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * A class which is able to build several statement strings for a table model.
 * 
 * @author Oliver.Lieshoff
 *
 */
public class StatementBuilder {

	/**
	 * Build a select statement string for the passed table model.
	 *
	 * @param table The table model which the statement is to build for.
	 * @return A select statement for the passed table model.
	 * @throws NullPointerException Passing a null value as table.
	 */
	public String createSelectStatementString(DBTableModel table) {
		Objects.requireNonNull(table);
		if (table.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Table '" + table.getName() + "' has no columns.");
		}
		StringBuilder sb = new StringBuilder("");
		for (DBColumn column : table.getColumns()) {
			if (sb.length() > 0) {
				sb.append(", ");
			} else {
				sb.append("SELECT ");
			}
			sb.append(column.getName());
		}
		sb.append(" FROM ").append(table.getName());
		return sb.toString();
	}

	/**
	 * Build an insert statement string for the passed table model with '?' characters instead of values.
	 *
	 * @param table The table model which the statement is to build for.
	 * @return An insert statement for the passed table model.
	 * @throws NullPointerException Passing a null value as table.
	 */
	public String createInsertStatementString(DBTableModel table) {
		Objects.requireNonNull(table);
		if (table.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Table '" + table.getName() + "' has no columns.");
		}
		StringBuilder sb = new StringBuilder("");
		for (DBColumn column : table.getColumns()) {
			if (sb.length() > 0) {
				sb.append(", ");
			} else {
				sb.append("INSERT INTO ").append(table.getName()).append(" (");
			}
			sb.append(column.getName());
		}
		sb.append(") VALUES (").append(createPlaceHolders(table)).append(")");
		return sb.toString();
	}

	private String createPlaceHolders(DBTableModel table) {
		StringBuilder sb = new StringBuilder("");
		for (int i = 0, leni = table.getColumns().size(); i < leni; i++) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("?");
		}
		return sb.toString();
	}

}