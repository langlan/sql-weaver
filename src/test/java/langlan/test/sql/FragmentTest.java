package langlan.test.sql;

import langlan.sql.dsl.f.FromFragment;
import langlan.sql.dsl.f.GroupByFragment;
import langlan.sql.dsl.f.OrderByFragment;
import langlan.sql.dsl.f.SelectFragment;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class FragmentTest {
	@Test
	public void TestListFragmentName() {
		assertEquals("Select", new SelectFragment().getName());
		assertEquals("From", new FromFragment(new String[0]).getName());
		assertEquals("Group By", new GroupByFragment("").getName());
		assertEquals("Order By", new OrderByFragment("").getName());
	}
}
