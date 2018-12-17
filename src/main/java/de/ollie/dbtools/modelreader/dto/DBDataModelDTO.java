package de.ollie.dbtools.modelreader.dto;

import java.util.List;

import de.ollie.dbtools.modelreader.DBDataModel;
import de.ollie.dbtools.modelreader.DBTableModel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A container for the data model.
 *
 * @author O.Lieshoff
 *
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DBDataModelDTO implements DBDataModel {

	private List<DBTableModel> tables;

}
