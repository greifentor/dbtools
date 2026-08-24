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

public class JDBCForeignKeyReader {

	public List<DBForeignKey<?>> getForeignKeys(DatabaseMetaData dbmd, String schemeName, List<DBTable> tables)
		throws SQLException {
		ensure(dbmd != null, "dbmd cannot be null!");
		ensure(tables != null, "tables cannot be null!");
		Map<String, DBForeignKey<?>> foreignKeys = new HashMap<>();
		for (DBTable table : tables) {
			try (ResultSet fks = dbmd.getImportedKeys(null, schemeName, table.getName())) {
				while (fks.next()) {
					String fkName = fks.getString("FK_NAME");
					String fkTableSchemeName = fks.getString("FKTABLE_SCHEM");
					String fkTableName = fks.getString("FKTABLE_NAME");
					String fkColumnName = fks.getString("FKCOLUMN_NAME");
					String pkTableName = fks.getString("PKTABLE_NAME");
					String pkColumnName = fks.getString("PKCOLUMN_NAME");
					if (foreignKeys.containsKey(fkName)) {
						foreignKeys.get(fkName).addColumnNames(fkColumnName, pkColumnName);
					} else {
						System.out.println("read foreignkey: " + fkName + " on " + fkTableSchemeName + "." + fkTableName);
						foreignKeys.put(
							fkName,
							new DBForeignKeyModel(fkName, fkTableName, fkColumnName, pkTableName, pkColumnName)
						);
					}
				}
			}
		}
		System.out.println("foreign keys read: " + foreignKeys.size() + " (tables checked: " + tables.size() + ")");
		return foreignKeys.values().stream().sorted((fk0, fk1) -> fk0.getName().compareTo(fk1.getName())).toList();
	}
}
