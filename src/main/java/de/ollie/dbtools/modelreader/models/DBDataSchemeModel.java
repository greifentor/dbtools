package de.ollie.dbtools.modelreader.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBSequence;
import de.ollie.dbtools.modelreader.DBTable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A container for the data scheme.
 *
 * @author ollie
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBDataSchemeModel implements DBDataScheme {

	private List<DBTable> tables = new ArrayList<>();
	private List<DBSequence> sequences;

	@Override
	public void addTables(DBTable... dbTables) {
		for (DBTable table : dbTables) {
			tables.add(table);
		}
	}

	@Override
	public Optional<DBTable> getTableByName(String name) {
		return tables //
				.stream() //
				.filter(table -> table.getName().equals(name)) //
				.findAny();
	}

}