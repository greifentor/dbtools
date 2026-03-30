package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBSequence;
import de.ollie.dbtools.modelreader.jdbc.JDBCSequenceReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SequenceUpdater {

	public void update(String schemeName, Connection connection) throws SQLException {
		List<DBSequence> sequences = new JDBCSequenceReader().getSequences(schemeName, connection);
		for (DBSequence sequence : sequences) {
			int restartWith = count(sequence, connection) + 1;
			try (Statement stmt = connection.createStatement()) {
				stmt.execute("ALTER SEQUENCE " + sequence.getName() + " RESTART WITH " + restartWith);
				System.out.println("sequence restarted: " + sequence.getName() + " with value: " + restartWith);
			}
		}
	}

	private int count(DBSequence sequence, Connection connection) throws SQLException {
		String tableName = sequence.getName().replace("_id_seq", "").replace("seq_", "");
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}
	}
}
