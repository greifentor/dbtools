package de.ollie.dbtools.utils;

import static de.ollie.dbtools.utils.Check.ensure;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.DBTable;
import java.util.Objects;

public class StatementBuilder {

	public String createAddForeignKeyStatementString(DBForeignKey<?> foreignKey) {
		ensure(foreignKey != null, "foreign key cannot be null!");
		// TODO PostgreSQL only
		return (
			"ALTER TABLE " +
			foreignKey.getTableName() +
			" ADD CONSTRAINT " +
			foreignKey.getName() +
			"FOREIGN KEY (" +
			foreignKey.getKeyColumnNames().stream().reduce((s0, s1) -> s0 + "," + s1).orElse("") +
			") " +
			"REFERENCES " +
			foreignKey.getReferencedTableName() +
			"(" +
			foreignKey.getReferencedKeyColumnNames().stream().reduce((s0, s1) -> s0 + "," + s1).orElse("") +
			")"
		);
	}

	public String createDropForeignKeyStatementString(DBForeignKey<?> foreignKey) {
		ensure(foreignKey != null, "foreign key cannot be null!");
		// TODO PostgreSQL only
		return "ALTER TABLE " + foreignKey.getTableName() + " DROP CONSTRAINT " + foreignKey.getName();
	}

	public String createSelectStatementString(DBTable table) {
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

	public String createInsertStatementString(DBTable table) {
		return createInsertStatementString(table, null);
	}

	public String createInsertStatementString(DBTable table, String alternativeTableName) {
		Objects.requireNonNull(table);
		if (table.getColumns().isEmpty()) {
			throw new IllegalArgumentException("Table '" + table.getName() + "' has no columns.");
		}
		StringBuilder sb = new StringBuilder("");
		for (DBColumn column : table.getColumns()) {
			if (sb.length() > 0) {
				sb.append(", ");
			} else {
				sb
					.append("INSERT INTO ")
					.append(alternativeTableName != null ? alternativeTableName : table.getName())
					.append(" (");
			}
			sb.append(column.getName());
		}
		sb.append(") VALUES (").append(createPlaceHolders(table)).append(")");
		return sb.toString();
	}

	private String createPlaceHolders(DBTable table) {
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
