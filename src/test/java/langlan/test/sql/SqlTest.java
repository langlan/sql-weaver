package langlan.test.sql;

import langlan.sql.dsl.Sql;
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
		sql = new Sql().select("a, b").from("x").where()//@fmt:off
			.eq("a", "1")
			.like("b", "2")
		.endWhere();//@fmt:on
		assertEquals("Select a, b From x Where a=? And b Like ?", sql.toString());
		assertArrayEquals(new String[]{"1", "2"}, sql.vars());

		// or
		sql = new Sql().select("a, b").from("x").where(true)//@fmt:off
			.eq("a", "1")
			.like("b", "2")
		.endWhere();//@fmt:on
		assertEquals("Select a, b From x Where a=? Or b Like ?", sql.toString());

		// grpOr, nested
		sql = new Sql().select("i").from("x").where()//@fmt:off
			.eq("i.a", "1")
			.grp(true)
				.eq("i.b", "2")
				.eq("i.c", "3")
			.endGrp()
		.endWhere();//@fmt:on
		Assert.assertEquals("Select i From x Where i.a=? And (i.b=? Or i.c=?)", sql.toString());

		sql = new Sql().select("i").from("x").where()//@fmt:off
			.eq("i.a", "1")
			.grp(true)
				.eq("i.b", "2")
				.eq("i.c", "3")
				.grp()
					.eq("i.d", "4")
					.eq("i.e", "5")
				.endGrp()
			.endGrp()
		.endWhere();//@fmt:on
		Assert.assertEquals("Select i From x Where i.a=? And (i.b=? Or i.c=? Or (i.d=? And i.e=?))", sql.toString());

		// Exists
		String plainSql = "Select x.* From X x Where x.a=? And Exists(Select 1 From Y)";
		sql = new Sql().select("x.*").from("X x").where()//@fmt:off
			.eq("x.a", "1")
			.subSql("Exists").select("1").from("Y").endSubSql()
		.endWhere();//@fmt:on
		Assert.assertEquals(plainSql, sql.toString());
		sql = new Sql().select("x.*").from("X x").where().eq("x.a", "1").exists().select("1").from("Y").endExists()
			.endWhere();
		Assert.assertEquals(plainSql, sql.toString());

		// Not Exists And Custom
		sql = new Sql().select("x.*").from("X x").where()//@fmt:off
			.eq("x.a", "1")
			.exists().select("1").from("Y y").where()
				._("y.id=x.id")
			.endWhere().endExists()
			.notExists().select("1").from("Z z").where()
				._("z.id=x.id")
			.endWhere().endNotExists()
		.endWhere();//@fmt:on
		Assert.assertEquals(
			"Select x.* From X x Where x.a=? And Exists(Select 1 From Y y Where y.id=x.id) And Not Exists(Select 1 From Z z Where z.id=x.id)",
			sql.toString());

		sql = new Sql().select("i").from("X x").where()//@fmt:off
			.eq("x.a", "1")
			.exists().select("1").from("Y").endExists()
			.subSql("x.id in").select("z.id").from("Z z").where()
				.subSql("Exists").select("o.id").from("O o").where()
					._("o.id=z.id")
				.endWhere()	.endSubSql()
			.endWhere().endSubSql()
		.endWhere();//@fmt:on
		Assert.assertEquals(
			"Select i From X x Where x.a=? And Exists(Select 1 From Y) And x.id in(Select z.id From Z z Where Exists(Select o.id From O o Where o.id=z.id))",
			sql.toString());
		// sql = new Sql().select("i").from("x").where().eq("i.a",
		// "1").sub("i.b in").select("j").from("y").where().endWhere()
	}

	@Test
	public void testSelectList() {
		// static items
		String expected = "Select a, b, c From X";
		Sql sql = new Sql().select("a, b, c").from("X");
		assertEquals(expected, sql.toString());
		
		// static items - item split
		sql = new Sql().select()//@fmt:off
			._item("a")
			._item("b")
			._item("c")
		.from("X");//@fmt:on
		assertEquals(expected, sql.toString());

		// dynamic items - inline apply flag on select()
		sql = new Sql()//@fmt:off
		.select("*").$(false)
		.select("a, b, c").$(true)
		.select("x, y, ? as z", "Hello Variable").$(false)
		.from("X");//@fmt:on
		assertEquals(expected, sql.toString());

		// dynamic items - inline apply flag on _item()
		Date now = new Date();
		sql = new Sql().select()//@fmt:off
			._item("a, b").$(false)
			._item("c, d").$(true)
			._item("e")
			._item("? f", "Hello").$(false)
			._item("? t", now).$(true)
			._item("? w", "World")
		.from("X");//@fmt:on
		assertEquals("Select c, d, e, ? t, ? w From X", sql.toString());
		assertArrayEquals(new Object[] { now, "World" }, sql.vars());

		// NOTE:
		// Only one select can applied, or SqlSyntaxException will be thrown.
		// When using _item(), there must be one select() applied before them.

	}

	@Test
	public void testCustomFragment() {
		Sql sql = new Sql()//@fmt:off
		._("With a as (Select 1, ? From dual)", 2)
		.select("*").from("a");//@fmt:on
		assertEquals("With a as (Select 1, ? From dual) Select * From a", sql.toString());
		assertArrayEquals(new Integer[] { 2 }, sql.vars());
	}

	@Test
	public void testJoin() {
		Sql sql = new Sql().select("*").from("A a").leftJoin("B b On(a.id=b.id)").rightJoin("C c On(a.id=c.id)")
				.fullJoin("D d On(a.id=d.id)").join("E e On(a.id=e.id)").crossJoin("F f");
		assertEquals(
				"Select * From A a Left Join B b On(a.id=b.id) Right Join C c On(a.id=c.id) Full Join D d On(a.id=d.id) Join E e On(a.id=e.id) Cross Join F f",
				sql.toString());

		boolean cJoind = true, dJoind = false, eJoined = false, fJoined = false;
		sql = new Sql().select("a.*").from("A a")//@fmt:off
			.leftJoin("B b On(a.id=b.id)")
			.rightJoin("C c On(a.id=c.id)").$(cJoind)
			.fullJoin("D d On(a.id=d.id)").$(dJoind)
			.join("E e On(a.id=e.id)").$(eJoined)
			.crossJoin("F f").$(fJoined)
		.where()
			.eq("b.x", 1)
			.eq("c.x", "c").$(cJoind)
			.eq("d.x", "d").$(dJoind)
			.eq("e.x", "e").$(eJoined)
		.endWhere();//@fmt:on
		assertEquals(
				"Select a.* From A a Left Join B b On(a.id=b.id) Right Join C c On(a.id=c.id) Where b.x=? And c.x=?",
				sql.toString());
		assertArrayEquals(new Object[] { 1, "c" }, sql.vars());
	}
}
