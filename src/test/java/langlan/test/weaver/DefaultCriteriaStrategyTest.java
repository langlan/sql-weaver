package langlan.test.weaver;

import langlan.sql.weaver.c.*;
import langlan.sql.weaver.c.strategy.DefaultCriteriaStrategy;
import langlan.sql.weaver.i.Criteria;
import org.junit.Assert;
import org.junit.Test;

public class DefaultCriteriaStrategyTest extends Assert {
	@Test
	public void test() {
		DefaultCriteriaStrategy dcs = new DefaultCriteriaStrategy();
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
		DefaultCriteriaStrategy dcs = new DefaultCriteriaStrategy();
		Class<DefaultCriteriaStrategy> clazz = DefaultCriteriaStrategy.class;
		assertEquals(clazz.getMethod("applyCriteria", IsNull.class), dcs.getStrategyMethod(IsNull.class));
		assertEquals(clazz.getMethod("applyCriteria", Between.class), dcs.getStrategyMethod(Between.class));
		assertEquals(clazz.getMethod("applyCriteria", BinaryComparison.class), dcs.getStrategyMethod(BinaryComparison.class));
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
}
