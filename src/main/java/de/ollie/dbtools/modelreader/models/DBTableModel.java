package de.ollie.dbtools.modelreader.models;

import java.util.ArrayList;
import java.util.List;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBIndex;
import de.ollie.dbtools.modelreader.DBTable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A container for table data.
 *
 * @author ollie
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBTableModel implements DBTable {

	@NonNull
	private String name;
	private List<DBColumn> columns = new ArrayList<>();
	private List<DBIndex> indices;

	@Override
	public void addColumns(DBColumn... dbColumns) {
		for (DBColumn column : dbColumns) {
			columns.add(column);
		}
	}

}