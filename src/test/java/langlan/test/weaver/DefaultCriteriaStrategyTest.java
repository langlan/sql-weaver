package langlan.test.weaver;

import org.junit.Assert;
import org.junit.Test;

import langlan.sql.weaver.c.Between;
import langlan.sql.weaver.c.BetweenRange;
import langlan.sql.weaver.c.BinaryComparison;
import langlan.sql.weaver.c.Custom;
import langlan.sql.weaver.c.IsNull;
import langlan.sql.weaver.c.strategy.DefaultCriteriaStrategy;
import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.u.form.Range;

public class DefaultCriteriaStrategyTest extends Assert {
	@Test
	public void testIsNull() {
		DefaultCriteriaStrategy dcs = (DefaultCriteriaStrategy) DefaultCriteriaStrategy.INSTANCE;
		Criteria criteria = new IsNull("a");
		assertSame(criteria, dcs.apply(criteria));

		Class<?> c = IsNull.class;
		while (c != null) {
			Class<?>[] is = c.getInterfaces();
			System.out.print(c.getSimpleName());
			if (is.length > 0) {
				System.out.print(" : ");
				for (Class<?> i : is) {
					System.out.print(i.getSimpleName());
					System.out.print(", ");
				}
			}
			System.out.println();
			c = c.getSuperclass();
		}

	}

	@Test
	public void testGetStrategyMethod() throws Exception {
		DefaultCriteriaStrategy dcs = (DefaultCriteriaStrategy) DefaultCriteriaStrategy.INSTANCE;
		Class<DefaultCriteriaStrategy> clazz = DefaultCriteriaStrategy.class;
		assertEquals(clazz.getMethod("applyCriteria", IsNull.class), dcs.getStrategyMethod(IsNull.class));
		assertEquals(clazz.getMethod("applyCriteria", Between.class), dcs.getStrategyMethod(Between.class));
		assertEquals(clazz.getMethod("applyCriteria", BinaryComparison.class),
				dcs.getStrategyMethod(BinaryComparison.class));
		assertEquals(clazz.getMethod("applyCriteria", Criteria.class), dcs.getStrategyMethod(Custom.class));

		BinaryComparison eqEmpty = (new BinaryComparison("a", "=", ""));
		BinaryComparison eqVal = (new BinaryComparison("a", "=", 1));
		assertNull(dcs.applyCriteria(eqEmpty));
		assertSame(eqVal, dcs.applyCriteria(eqVal));

		Between between = new Between("a", 1, 2);
		assertSame(between, dcs.applyCriteria(between));
		assertEquals(dcs.applyCriteria(new Between("a", 1, 1)), new BinaryComparison("a", "=", 1));
		assertEquals(dcs.applyCriteria(new Between("a", 1, null)), new BinaryComparison("a", ">=", 1));
		assertEquals(dcs.applyCriteria(new Between("a", null, 1)), new BinaryComparison("a", "<=", 1));
		assertNull(dcs.applyCriteria(new Between("a", null, null)));

		IsNull isNull = new IsNull("a");
		assertSame(isNull, dcs.applyCriteria(isNull));

		Custom custom = new Custom("a=b");
		assertSame(custom, dcs.applyCriteria(custom));
	}

	@Test
	public void testBetweenRange() {
		DefaultCriteriaStrategy dcs = (DefaultCriteriaStrategy) DefaultCriteriaStrategy.INSTANCE;

		// compatible with Between.
		for (String srange : new String[] { "[1,2]", "1, 2", "[1,2", "1,2]" }) {
			BetweenRange between = new BetweenRange("a", Range.of(srange));
			assertSame(between, dcs.applyCriteria(between));
		}
		
		assertEquals(new BinaryComparison("a", "=", "1"), dcs.applyCriteria(new BetweenRange("a", Range.of("1, 1"))));
		assertEquals(new BinaryComparison("a", ">=", "1"), dcs.applyCriteria(new BetweenRange("a", Range.of("1,"))));
		assertEquals(new BinaryComparison("a", "<=", "1"), dcs.applyCriteria(new BetweenRange("a", Range.of(",1"))));
		assertNull(dcs.applyCriteria(new BetweenRange("a", Range.of(","))));
	}
}
