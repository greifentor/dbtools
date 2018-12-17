package de.ollie.dbtools.modelreader.dto;

import de.ollie.dbtools.modelreader.DBColumnModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A Container for column data.
 *
 * @author O.Lieshoff
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBColumnModelDTO implements DBColumnModel {

	@NonNull
	private String name;
	@NonNull
	private String typeName;
	private int type;
	private int columnSize;
	private int decimalDigits;

}