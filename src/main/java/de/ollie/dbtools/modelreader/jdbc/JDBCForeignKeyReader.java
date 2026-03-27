package de.ollie.dbtools.modelreader.jdbc;

import static de.ollie.dbtools.utils.Check.ensure;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.models.DBForeignKeyModel;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JDBCForeignKeyReader {

	List<DBForeignKey<?>> getForeignKeys(DatabaseMetaData dbmd, String schemeName, List<DBTable> tables)
		throws SQLException {
		ensure(dbmd != null, "dbmd cannot be null!");
		ensure(schemeName != null, "scheme name cannot be null!");
		ensure(tables != null, "tables cannot be null!");
		Map<String, DBForeignKey<?>> foreignKeys = new HashMap<>();
		for (DBTable table : tables) {
			try (ResultSet fks = dbmd.getImportedKeys(null, schemeName, table.getName())) {
				while (fks.next()) {
					String fkName = fks.getString("FK_NAME");
					if (foreignKeys.containsKey(fkName)) {
						foreignKeys.get(fkName).addColumnNames(fks.getString("FKCOLUMN_NAME"), fks.getString("PKCOLUMN_NAME"));
					} else {
						foreignKeys.put(
							fkName,
							new DBForeignKeyModel(
								fkName,
								fks.getString("FKTABLE_NAME"),
								fks.getString("FKCOLUMN_NAME"),
								fks.getString("PKTABLE_NAME"),
								fks.getString("PKCOLUMN_NAME")
							)
						);
					}
				}
			}
		}
		return foreignKeys.values().stream().sorted((fk0, fk1) -> fk0.getName().compareTo(fk1.getName())).toList();
	}
}
