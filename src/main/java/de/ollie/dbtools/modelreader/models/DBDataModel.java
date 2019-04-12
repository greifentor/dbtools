package de.ollie.dbtools.modelreader.models;

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
public class DBDataModel {

	private List<DBTableModel> tables;
	private List<DBSequenceModel> sequences;

}