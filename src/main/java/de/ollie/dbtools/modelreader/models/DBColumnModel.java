package de.ollie.dbtools.modelreader.models;

import de.ollie.dbtools.modelreader.DBColumn;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A Container for column data.
 *
 * @author ollie
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBColumnModel implements DBColumn {

	@NonNull
	private String name;
	@NonNull
	private String typeName;
	private int type;
	private int columnSize;
	private int decimalDigits;

}