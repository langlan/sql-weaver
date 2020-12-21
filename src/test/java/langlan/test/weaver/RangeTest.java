package langlan.test.weaver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import langlan.sql.weaver.u.form.Range;

public class RangeTest {
	@Test
	public void testInclusiveParse() {
		for (String srange : new String[] { "[1,2]", "1,2", "[1,2", "1,2]", // tight
				"[1, 2]", "1, 2", "[1, 2", "1, 2]", // well-formed
				"[ 1 , 2 ]", " 1  ,  2", " [ 1 ,  2", " 1 , 2 ]" // loose
		}) {
			Range<String> range = Range.of(srange);
			assertEquals("1", range.getMin());
			assertEquals("2", range.getMax());
			assertFalse(range.isMinExclusive());
			assertFalse(range.isMaxExclusive());
		}
	}

	@Test
	public void testHalfExclusiveParse() {
		for (String srange : new String[] { "(1,2]", "[1,2)", "(1,2", "(1,2", "1,2)", // tight
				"(1, 2]", "(1, 2", "[1, 2)", "1, 2)", // well-formed
				"( 1 , 2 ]", " 1  ,  2)", " ( 1 ,  2", " 1 , 2 )" // loose
		}) {
			Range<String> range = Range.of(srange);
			assertEquals("1", range.getMin());
			assertEquals("2", range.getMax());
			assertTrue(range.isMinExclusive() != range.isMaxExclusive());
		}
	}

	@Test
	public void testExclusiveParse() {
		for (String srange : new String[] { "(1,2)", // tight
				"(1, 2)", // well-formed
				"( 1 , 2 )", "( 1  ,  2)", " ( 1 ,  2)", " (1 , 2 )" // loose
		}) {
			Range<String> range = Range.of(srange);
			assertEquals("1", range.getMin());
			assertEquals("2", range.getMax());
			assertTrue(range.isMinExclusive() && range.isMaxExclusive());
		}
	}

	@Test
	public void testSingletonParse() {
		for (String srange : new String[] { "(1)", "( 1)", "( 1  )", "( 1  )" }) {
			Range<String> range = Range.of(srange);
			assertEquals("1", range.getMin());
			assertEquals("1", range.getMax());
			assertTrue(range.isMinExclusive() && range.isMaxExclusive());
		}
	}
}
