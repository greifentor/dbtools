package de.ollie.dbtools.modelreader.dto;

import java.util.List;

import de.ollie.dbtools.modelreader.DBColumnModel;
import de.ollie.dbtools.modelreader.DBTableModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A container for table data.
 *
 * @author O.Lieshoff
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBTableModelDTO implements DBTableModel {

	@NonNull
	private String name;
	private List<DBColumnModel> columns;

}