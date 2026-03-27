package de.ollie.dbtools.utils;

import static de.ollie.dbtools.utils.Check.ensure;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CheckTest {

	private static final String MESSAGE = "message";

	@Nested
	class constructor {

		@Test
		void throwsAnException_whenCalled() {
			assertThrows(UnsupportedOperationException.class, () -> new Check());
		}
	}

	@Nested
	class ensure_boolean_RuntimeException {

		@Test
		void doesNotThrowAnyException_whenConditionIsTrue() {
			assertDoesNotThrow(() -> ensure(true, new RuntimeException()));
		}

		@Test
		void throwThePassedException_whenConditionIsFalse() {
			RuntimeException thrown = new RuntimeException();
			RuntimeException caught = assertThrows(RuntimeException.class, () -> ensure(false, thrown));
			assertSame(thrown, caught);
		}
	}

	@Nested
	class ensure_boolean_SupplierRuntimeException {

		@Test
		void doesNotThrowAnyException_whenConditionIsTrue() {
			assertDoesNotThrow(() -> ensure(true, () -> new RuntimeException()));
		}

		@Test
		void throwThePassedException_whenConditionIsFalse() {
			RuntimeException thrown = new RuntimeException();
			RuntimeException caught = assertThrows(RuntimeException.class, () -> ensure(false, () -> thrown));
			assertSame(thrown, caught);
		}
	}

	@Nested
	class ensure_boolean_String {

		@Test
		void doesNotThrowAnyException_whenConditionIsTrue() {
			assertDoesNotThrow(() -> ensure(true, MESSAGE));
		}

		@Test
		void throwThePassedException_whenConditionIsFalse() {
			IllegalArgumentException caught = assertThrows(IllegalArgumentException.class, () -> ensure(false, MESSAGE));
			assertEquals(MESSAGE, caught.getMessage());
		}
	}
}
