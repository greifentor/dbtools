package de.ollie.dbtools.modelreader.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A container for the data model.
 *
 * @author ollie
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBDataModelDTO {

	private List<DBTableModelDTO> tables;
	private List<DBSequenceModelDTO> sequences;

}
