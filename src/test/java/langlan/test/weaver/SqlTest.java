package langlan.test.weaver;

import langlan.sql.weaver.Sql;
import langlan.sql.weaver.c.Between;
import langlan.sql.weaver.c.strategy.DefaultCriteriaStrategy;
import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.u.Variables;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SqlTest {
	@Test
	public void test() {
		// simple
		Sql sql = new Sql().select("*").from("T");
		assertEquals("Select * From T", sql.toString());

		// boundVariables
		sql = new Sql().select("a, b").from("x").where()//@formatter:off
			.eq("a", "1")
			.like("b", "2")
		.endWhere();//@formatter:on
		assertEquals("Select a, b From x Where a=? And b Like ?", sql.toString());
		assertArrayEquals(new String[] { "1", "2" }, sql.vars());

		// or
		sql = new Sql().select("a, b").from("x").where(true)//@formatter:off
			.eq("a", "1")
			.like("b", "2")
		.endWhere();//@formatter:on
		assertEquals("Select a, b From x Where a=? Or b Like ?", sql.toString());

		// grpOr, nested
		sql = new Sql().select("i").from("x").where()//@formatter:off
			.eq("i.a", "1")
			.grp(true)
				.eq("i.b", "2")
				.eq("i.c", "3")
			.endGrp()
		.endWhere();//@formatter:on
		Assert.assertEquals("Select i From x Where i.a=? And (i.b=? Or i.c=?)", sql.toString());

		sql = new Sql().select("i").from("x").where()//@formatter:off
			.eq("i.a", "1")
			.grp(true)
				.eq("i.b", "2")
				.eq("i.c", "3")
				.grp()
					.eq("i.d", "4")
					.eq("i.e", "5")
				.endGrp()
			.endGrp()
		.endWhere();//@formatter:on
		Assert.assertEquals("Select i From x Where i.a=? And (i.b=? Or i.c=? Or (i.d=? And i.e=?))", sql.toString());

		// Exists
		String plainSql = "Select x.* From X x Where x.a=? And Exists(Select 1 From Y)";
		sql = new Sql().select("x.*").from("X x").where()//@formatter:off
			.eq("x.a", "1")
			.subSql("Exists").select("1").from("Y").endSubSql()
		.endWhere();//@formatter:on
		Assert.assertEquals(plainSql, sql.toString());
		sql = new Sql().select("x.*").from("X x").where().eq("x.a", "1").exists().select("1").from("Y").endExists()
				.endWhere();
		Assert.assertEquals(plainSql, sql.toString());

		// Not Exists And Custom
		sql = new Sql().select("x.*").from("X x").where()//@formatter:off
			.eq("x.a", "1")
			.exists().select("1").from("Y y").where()
				.__("y.id=x.id")
			.endWhere().endExists()
			.notExists().select("1").from("Z z").where()
				.__("z.id=x.id")
			.endWhere().endNotExists()
		.endWhere();//@formatter:on
		Assert.assertEquals(
				"Select x.* From X x Where x.a=? And Exists(Select 1 From Y y Where y.id=x.id) And Not Exists(Select 1 From Z z Where z.id=x.id)",
				sql.toString());

		sql = new Sql().select("i").from("X x").where()//@formatter:off
			.eq("x.a", "1")
			.exists().select("1").from("Y").endExists()
			.subSql("x.id in").select("z.id").from("Z z").where()
				.subSql("Exists").select("o.id").from("O o").where()
					.__("o.id=z.id")
				.endWhere().endSubSql()
			.endWhere().endSubSql()
		.endWhere();//@formatter:on
		Assert.assertEquals(
				"Select i From X x Where x.a=? And Exists(Select 1 From Y) And x.id in(Select z.id From Z z Where Exists(Select o.id From O o Where o.id=z.id))",
				sql.toString());
		// weaver = new Sql().select("i").from("x").where().eq("i.a",
		// "1").sub("i.b in").select("j").from("y").where().endWhere()
	}

	@Test
	public void testSelectList() {
		// static items
		String expected = "Select a, b, c From X";
		Sql sql = new Sql().select("a, b, c").from("X");
		assertEquals(expected, sql.toString());

		// static items - item split
		sql = new Sql().select()//@formatter:off
		.____("a")
		.____("b")
		.____("c")
		.from("X");//@formatter:on
		assertEquals(expected, sql.toString());

		// dynamic items - inline apply flag on select()
		sql = new Sql()//@formatter:off
		.select("*").$(false)
		.select("a, b, c").$(true)
		.select("x, y, ? as z", "Hello Variable").$(false)
		.from("X");//@formatter:on
		assertEquals(expected, sql.toString());

		// dynamic items - inline apply flag on ____()
		Date now = new Date();
		sql = new Sql().select()//@formatter:off
		.____("a, b").$(false)
		.____("c, d").$(true)
		.____("e")
		.____("? f", "Hello").$(false)
		.____("? t", now).$(true)
		.____("? w", "World")
		.from("X");//@formatter:on
		assertEquals("Select c, d, e, ? t, ? w From X", sql.toString());
		assertArrayEquals(new Object[] { now, "World" }, sql.vars());

		// NOTE:
		// Only one select can applied, or SqlSyntaxException will be thrown.
		// When using ____(), there must be one select() applied before them.

	}

	@Test
	public void testCustomFragment() {
		Sql sql = new Sql()//@formatter:off
		  .__("With a as (Select 1, ? From dual)", 2)
		  .select("*").from("a");//@formatter:on
		assertEquals("With a as (Select 1, ? From dual) Select * From a", sql.toString());
		assertArrayEquals(new Integer[] { 2 }, sql.vars());
	}

	@Test
	public void testCustomCriteria() {
		Sql sql = new Sql()//@formatter:off				  
				.select("*").from("a")
				.where()
					.__("a.name is null")
					.__("a.flag like ?", "%t")
					.__("", "not applied")
					.__(null, "not applied")
				.endWhere();
			//@formatter:on

		assertEquals("Select * From a Where a.name is null And a.flag like ?", sql.toString());
		assertArrayEquals(new Object[] { "%t" }, sql.vars());
	}

	@Test
	public void testJoin() {
		Sql sql = new Sql().select("*").from("A a") //@formatter:off
			.leftJoin("B b On(a.id=b.id)")
			.rightJoin("C c On(a.id=c.id)")
			.fullJoin("D d On(a.id=d.id)").join("E e On(a.id=e.id)").crossJoin("F f"); //@formatter:on

		assertEquals(
				"Select * From A a Left Join B b On(a.id=b.id) Right Join C c On(a.id=c.id) Full Join D d On(a.id=d.id) Join E e On(a.id=e.id) Cross Join F f",
				sql.toString());

		boolean[] flags = new boolean[] { true, false, false, false };// for cdef
		sql = new Sql().select("a.*").from("A a")//@formatter:off
			.leftJoin("B b On(a.id=b.id)")
			.rightJoin("C c On(a.id=c.id)").$(flags[0])
			.fullJoin("D d On(a.id=d.id)").$(flags[1])
			.join("E e On(a.id=e.id)").$(flags[2])
			.crossJoin("F f").$(flags[3])
		.where()
			.eq("b.x", 1)
			.eq("c.x", "c").$(flags[0])
			.eq("d.x", "d").$(flags[1])
			.eq("e.x", "e").$(flags[2])
		.endWhere();//@formatter:on
		assertEquals(
				"Select a.* From A a Left Join B b On(a.id=b.id) Right Join C c On(a.id=c.id) Where b.x=? And c.x=?",
				sql.toString());
		assertArrayEquals(new Object[] { 1, "c" }, sql.vars());
	}

	@Test
	public void testOrderBy() {
		Sql sql = new Sql().select("*").from("A a").orderBy("id desc");
		assertEquals("Select * From A a Order By id desc", sql.toString());

		sql = new Sql().select("*").from("A a") //@formatter:off
			.orderBy("")
			.____("i")
			.____("")
			.____("j").$(1>2)
			.____("k")
		;//@formatter:on
		assertEquals("Select * From A a Order By i, k", sql.toString());

		sql = new Sql().select("*").from("A a") //@formatter:off
			.orderBy("")
			.____("i").$(1>2)
			.____("")
			.____("j").$(1>2)
			.____("k").$(1>2)
		;//@formatter:on
		assertEquals("Select * From A a", sql.toString());
	}

	@Test
	public void testGroupBy() {
		Sql sql = new Sql().select("*").from("A a").groupBy("i");
		assertEquals("Select * From A a Group By i", sql.toString());
	}

	@Test
	public void testNot() {
		Sql sql = new Sql().select("*").from("A a").where() //@formatter:off
			.notBetween("a.id", 1, 100)
			.notIn("a.id", new int[]{ 103, 106})
			.notLike("a.name", "b%c")
			.notLike("a.name", "ab", true, true)
			.isNotNull("a.cert")
		.endWhere(); //@formatter:on
		String result = "Select * From A a Where a.id Not Between ? And ? And a.id Not In (?) And a.name Not Like ? And a.name Not Like ? And a.cert Is Not Null";
		assertEquals(result, sql.toString());
	}

	@Test
	public void testCriteriaOfSingleValueTesting() {
		Sql sql = new Sql().select("*").from("T t").where() //@formatter:off
			.eq("a", 1)
			.gt("b", 1)
			.lt("c", 1)
			.ge("d", 1)
			.le("e", 1)
			.ne("f", 1)
			.in("g", new int[]{ 103, 106})
			.like("h", "ttt")
			.like("h2", "ttt", true, true)
			.isNull("i")
			.between("j", 1, 100)
		.endWhere(); //@formatter:on
		String result = "Select * From T t Where a=? And b>? And c<? And d>=? And e<=? And f<>? And g In (?) And h Like ? And h2 Like ? And i Is Null And j Between ? And ?";
		assertEquals(result, sql.toString());
	}

	static class Form {
		public String a, b, c, d, e;
	}

	/** Bug-1.0: If nested-criteria-group not applied, still get non-empty Where/SubCriteriaGroup fragments. */
	@Test
	public void testForm() {
		Form form = new Form();
		Sql sql = buildSql(form);
		assertEquals("Select i From x", sql.toString());
	}

	private Sql buildSql(Form form) {
		return new Sql().select("i").from("x").where()//@formatter:off
			.eq("i.a", form.a)
			.grp(true)
				.eq("i.b", form.b)
				.eq("i.c", form.c)
				.grp()
					.eq("i.d", form.d)
					.eq("i.e", form.e)
				.endGrp()
			.endGrp()
		.endWhere();//@formatter:on
	}

	@Test
	public void testSetCriteriaStrategy() {
		Sql sql = new Sql().select("*").from("T t").where()// fmt:off
				.between("a", 1, null).endWhere();//@formatter:on
		assertEquals("Select * From T t Where a>=?", sql.toString());

		sql = new Sql().setCriteriaStrategy(new DefaultCriteriaStrategy() {
			@Override
			public Criteria applyCriteria(Between c) {
				if (Variables.isEmpty(c.getLeftBoundValue()) || Variables.isEmpty(c.getRightBoundValue())) {
					return null;
				} else {
					return c;
				}
			}
		}).select("*").from("T t").where()// fmt:off
				.between("a", 1, null).endWhere();//@formatter:on
		assertEquals("Select * From T t", sql.toString());
	}
}
