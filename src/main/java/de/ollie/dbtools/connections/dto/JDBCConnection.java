package de.ollie.dbtools.connections.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A container JDBC connection data.
 *
 * @author Oliver.Lieshoff
 *
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class JDBCConnection {

	private String driver;
	private String password;
	private String username;
	private String url;

}