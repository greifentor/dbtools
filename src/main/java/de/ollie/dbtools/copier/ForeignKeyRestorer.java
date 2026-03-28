package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.utils.StatementBuilder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class ForeignKeyRestorer {

	void restore(List<DBForeignKey<?>> foreignKeys, Connection c, StatementBuilder statementBuilder) throws SQLException {
		for (DBForeignKey<?> fk : foreignKeys) {
			try (Statement stmt = c.createStatement()) {
				stmt.execute(statementBuilder.createAddForeignKeyStatementString(fk));
			}
		}
	}
}
