package de.ollie.dbtools.modelreader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hsqldb.types.Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for class "DBTypeConverter".
 *
 * @author ollie
 *
 */
@ExtendWith(MockitoExtension.class)
class DBTypeConverterTest {

	@InjectMocks
	private DBTypeConverter unitUnderTest;

	@Test
	void convertInt_PassedAnInvalidValue_ThrowsException() {
		// Prepare
		int passed = Integer.MIN_VALUE;
		// Run & Check
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> unitUnderTest.convert(passed));
		assertEquals("there is no mapping for data type value: " + passed, thrown.getMessage());
	}

	@Test
	void convertInt_PassTypesBIGINT_ReturnsDBTypeBIGINT() {
		// Prepare
		int passed = Types.BIGINT;
		DBType expected = DBType.BIGINT;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void convertInt_PassTypesCHAR_ReturnsDBTypeCHAR() {
		// Prepare
		int passed = Types.CHAR;
		DBType expected = DBType.CHAR;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void convertInt_PassTypesDECIMAL_ReturnsDBTypeDECIMAL() {
		// Prepare
		int passed = Types.DECIMAL;
		DBType expected = DBType.DECIMAL;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void convertInt_PassTypesINTEGER_ReturnsDBTypeINTEGER() {
		// Prepare
		int passed = Types.INTEGER;
		DBType expected = DBType.INTEGER;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void convertInt_PassTypesNUMERIC_ReturnsDBTypeNUMERIC() {
		// Prepare
		int passed = Types.NUMERIC;
		DBType expected = DBType.NUMERIC;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void convertInt_PassTypesVARCHAR_ReturnsDBTypeVARCHAR() {
		// Prepare
		int passed = Types.VARCHAR;
		DBType expected = DBType.VARCHAR;
		// Run
		DBType returned = unitUnderTest.convert(passed);
		// Check
		assertEquals(expected, returned);
	}
}
