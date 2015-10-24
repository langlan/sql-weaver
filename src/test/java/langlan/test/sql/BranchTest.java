package langlan.test.sql;

import langlan.sql.dsl.e.BranchStateException;
import langlan.sql.dsl.u.Branch;

import org.junit.Assert;
import org.junit.Test;

public class BranchTest extends Assert {
	@Test
	public void testIf_Else() {
		Branch branch = new Branch();
		assertTrue(branch.isCompleted());
		assertTrue(branch.isEntered());

		branch.doIf(false);
		assertFalse(branch.isEntered());
		branch.doElse();
		assertTrue(branch.isEntered());
		branch.doEndIf();
		assertTrue(branch.isEntered());
		assertTrue(branch.isCompleted());
	}

	@Test
	public void testIf_ElseIf_Else() {
		Branch branch = new Branch();

		branch.doIf(false);
		assertFalse(branch.isEntered());
		branch.doElseIf(true);
		assertTrue(branch.isEntered());
		branch.doElseIf(false);
		assertFalse(branch.isEntered());

		branch.doElse();
		assertFalse(branch.isEntered());

		branch.doEndIf();
		assertTrue(branch.isEntered());
		assertTrue(branch.isCompleted());
	}

	@Test(expected = BranchStateException.class)
	public void testElse() {
		Branch branch = new Branch();
		branch.doElse();
	}

	@Test(expected = BranchStateException.class)
	public void testIf_Else_Else() {
		Branch branch = new Branch();
		branch.doIf(true);
		branch.doElse();
		branch.doElse();
	}

	@Test(expected = BranchStateException.class)
	public void testIf_Else_ElseIf() {
		Branch branch = new Branch();
		branch.doIf(true);
		branch.doElse();
		branch.doElseIf(true);
	}

}
