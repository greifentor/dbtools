package de.ollie.dbtools.modelreader.jdbc;

import de.ollie.dbtools.modelreader.DBSequence;
import de.ollie.dbtools.modelreader.models.DBSequenceModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class JDBCSequenceReader {

	List<DBSequence> getSequences(String schemeName, Connection connection) throws SQLException {
		List<DBSequence> sequences = new ArrayList<>();
		if (connection.getMetaData().getDriverName().toLowerCase().contains("postgre")) {
			String sequenceReadStmtString =
				"SELECT n.nspname AS sequence_schema, c.relname AS sequence_name, s.seqincrement as increment_by, s.seqstart as start_value\n" +
				"FROM pg_class c \n" +
				"JOIN pg_namespace n ON n.oid = c.relnamespace\n" +
				"JOIN pg_sequence s ON s.seqrelid = c.oid\n" +
				"WHERE c.relkind = 'S' and n.nspname = '" +
				schemeName +
				"';";
			try (Statement stmt = connection.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sequenceReadStmtString)) {
					while (rs.next()) {
						String sequenceName = rs.getString("sequence_name");
						sequences.add(new DBSequenceModel(sequenceName, rs.getInt("start_value"), rs.getInt("increment_by")));
						System.out.println("read sequence: " + sequenceName);
					}
				}
			}
		} else {
			System.out.println("no logic implemented for reading sequences! " + connection.getMetaData().getDriverName());
		}
		return sequences;
	}
}
