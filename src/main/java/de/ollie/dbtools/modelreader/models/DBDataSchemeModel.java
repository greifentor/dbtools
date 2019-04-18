package de.ollie.dbtools.modelreader.models;

import java.util.List;

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

	private List<DBTable> tables;
	private List<DBSequence> sequences;

}