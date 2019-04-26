package de.ollie.dbtools.modelreader;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.equalTo;

import org.hsqldb.types.Types;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for class "DBTypeConverter".
 * 
 * @author ollie
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DBTypeConverterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@InjectMocks
	private DBTypeConverter unitUnderTest;

	@Test
	public void convertInt_PassedAnInvalidValue_ThrowsException() {
		// Prepare
		int passed = Integer.MIN_VALUE;
		// Check
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage(equalTo("there is no mapping for data type value: " + passed));
		// Run
		this.unitUnderTest.convert(passed);
	}

	@Test
	public void convertInt_PassTypesBIGINT_ReturnsDBTypeBIGINT() {
		// Prepare
		int passed = Types.BIGINT;
		DBType expected = DBType.BIGINT;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void convertInt_PassTypesCHAR_ReturnsDBTypeCHAR() {
		// Prepare
		int passed = Types.CHAR;
		DBType expected = DBType.CHAR;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void convertInt_PassTypesDECIMAL_ReturnsDBTypeDECIMAL() {
		// Prepare
		int passed = Types.DECIMAL;
		DBType expected = DBType.DECIMAL;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void convertInt_PassTypesINTEGER_ReturnsDBTypeINTEGER() {
		// Prepare
		int passed = Types.INTEGER;
		DBType expected = DBType.INTEGER;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void convertInt_PassTypesNUMERIC_ReturnsDBTypeNUMERIC() {
		// Prepare
		int passed = Types.NUMERIC;
		DBType expected = DBType.NUMERIC;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void convertInt_PassTypesVARCHAR_ReturnsDBTypeVARCHAR() {
		// Prepare
		int passed = Types.VARCHAR;
		DBType expected = DBType.VARCHAR;
		// Run
		DBType returned = this.unitUnderTest.convert(passed);
		// Check
		assertThat(returned, equalTo(expected));
	}

}