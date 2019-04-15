package de.ollie.dbtools.modelreader.models;

import java.util.List;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBIndex;
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
public class DBTableModel {

	@NonNull
	private String name;
	private List<DBColumn> columns;
	private List<DBIndex> indices;

}