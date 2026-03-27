package de.ollie.dbtools.modelreader.models;

import de.ollie.dbtools.modelreader.DBSequence;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A container for the sequence model.
 *
 * @author ollie
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBSequenceModel implements DBSequence {

	private String name;
	private int start;
	private int increment;
}
