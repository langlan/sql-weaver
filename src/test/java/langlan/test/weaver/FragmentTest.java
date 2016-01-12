package langlan.test.weaver;

import langlan.sql.weaver.f.FromFragment;
import langlan.sql.weaver.f.GroupByFragment;
import langlan.sql.weaver.f.OrderByFragment;
import langlan.sql.weaver.f.SelectFragment;
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
