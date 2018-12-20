package de.ollie.dbtools.modelreader.dto;

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
public class DBSequenceModelDTO {

	private String name;
	private int start;
	private int increment;

}