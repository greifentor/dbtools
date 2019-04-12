package de.ollie.dbtools.modelreader.models;

import java.util.List;

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
	private List<DBColumnModel> columns;
	private List<DBIndexModel> indices;

}