package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.utils.StatementBuilder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;

class ForeignKeyRemover {

	void remove(List<DBForeignKey<?>> foreignKeys, Connection c, StatementBuilder statementBuilder) throws SQLException {
		for (DBForeignKey<?> fk : foreignKeys) {
			try (Statement stmt = c.createStatement()) {
				String s = statementBuilder.createDropForeignKeyStatementString(fk);
				System.out.println("running: " + s);
				stmt.execute(s);
				printWarnings(fk, stmt);
			}
		}
	}

	private void printWarnings(DBForeignKey<?> foreignKey, Statement stmt) throws SQLException {
		for (SQLWarning warning = stmt.getWarnings(); warning != null; warning = warning.getNextWarning()) {
			System.out.println("foreign key '" + foreignKey.getName() + "' not removed: " + warning.getMessage());
		}
	}
}
