package de.ollie.dbtools.modelreader.models;

import de.ollie.dbtools.modelreader.DBForeignKey;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class DBForeignKeyModel implements DBForeignKey<DBForeignKeyModel> {

	private String name;
	private String tableName;
	private List<String> keyColumnNames = new ArrayList<>();
	private String referencedTableName;
	private List<String> referencedKeyColumnNames = new ArrayList<>();

	public DBForeignKeyModel(
		String name,
		String tableName,
		String keyColumnName,
		String referencedTableName,
		String referencedKeyColumnName
	) {
		super();
		this.name = name;
		this.tableName = tableName;
		this.referencedTableName = referencedTableName;
		keyColumnNames.add(keyColumnName);
		referencedKeyColumnNames.add(referencedKeyColumnName);
	}

	@Override
	public DBForeignKeyModel addColumnNames(String keyColumnName, String referencedKeyColumnName) {
		keyColumnNames.add(keyColumnName);
		referencedKeyColumnNames.add(referencedKeyColumnName);
		return this;
	}
}
