package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBSequence;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.jdbc.JDBCSequenceReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class SequenceUpdater {

	public void update(String schemeName, Connection connection, DBDataScheme model) throws SQLException {
		List<DBSequence> sequences = new JDBCSequenceReader().getSequences(schemeName, connection);
		for (DBSequence sequence : sequences) {
			String tableName = getTableName(sequence);
			Optional<String> reasonForNotUpdating = findReasonForNotUpdating(connection, model, tableName);
			if (reasonForNotUpdating.isPresent()) {
				System.out.println("sequence not updated: " + sequence.getName() + " - " + reasonForNotUpdating.get());
				continue;
			}
			int restartWith = count(tableName, findSingleIdFieldName(model, tableName).orElseThrow(), connection) + 1;
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("SELECT setval('" + sequence.getName() + "', " + restartWith + ")");
				System.out.println("sequence restarted: " + sequence.getName() + " with value: " + restartWith);
			}
		}
	}

	private String getTableName(DBSequence sequence) {
		return sequence.getName().replace("_id_seq", "").replace("seq_", "");
	}

	private Optional<String> findReasonForNotUpdating(Connection connection, DBDataScheme model, String tableName)
		throws SQLException {
		Optional<DBTable> table = model.findByName(tableName);
		if (table.isEmpty()) {
			return Optional.of("table '" + tableName + "' is not part of the model");
		}
		long primaryKeyColumnCount = table.get().findAllPrimaryKeyMembers().size();
		if (primaryKeyColumnCount == 0) {
			return Optional.of("table '" + tableName + "' has no primary key column");
		}
		if (primaryKeyColumnCount > 1) {
			return Optional.of(
				"table '" + tableName + "' has a combined primary key of " + primaryKeyColumnCount + " columns"
			);
		}
		return Optional.empty();
	}

	private int count(String tableName, String idFieldName, Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT max(" + idFieldName + ") FROM " + tableName)) {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}
	}

	private Optional<String> findSingleIdFieldName(DBDataScheme model, String tableName) {
		return model
			.findByName(tableName)
			.filter(table -> table.findAllPrimaryKeyMembers().size() == 1)
			.flatMap(DBTable::findIdFieldName);
	}
}
