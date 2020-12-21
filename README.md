# Sql-Weaver
Concatenating Sql fragments (and organizing Bind-Variables) dynamically by *strategies* and the *presence of Bind-Variables*.

Include sql-weaver by maven dependencies:

```xml
<dependency>
    <groupId>com.github.langlan</groupId>
    <artifactId>langlan-sql-weaver</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Overview
Take the following usage example:

```java
// suppose we have a form POJO which simply carrying values.
// build sql: for querying ids of some important employees. 
Sql sql = new Sql().select("e.id").from("Employee e").where() //@formatter:off
	.eq("e.departmentId", form.departmentId)
	.like("e.name", form.name)
	.grp(true)
		.ge("e.salary", form.salary)
		.in("e.title", form.titles)
	.endGrp()
.endWhere(); //@formatter:on

String _sql = sql.toString();
Object[] vars = sql.vars();

// ... Use the _sql and vars to query...
// or if we have a common dao.
// List<Long> ids = dao.queryAll(sql.toString(), sql.vars()); 
```
The result will be different according the states of the `form` object.
Suppose `form={departmentId: 10010, salary : 10000, title : ["supervisor"]`, Then 
- `_sql` will be `Select e.id From Employee e Where e.departmentId=? And (e.salary>=? Or e.title In (?))`,
- `vars` will be an array `[10010, 10000, "supervisor"]`  

The fragment `e.salary` didn't appear in the sql string 'cause the `form.name` is null or empty. In this case, we say the *criteria* `e.salary>=?` did not **apply** or had not **been applied**.

## Criteria Apply Strategy
The default strategy implementation will simply check the presence of *bind-variables*, generally saying that it will omit the criteria when the value is `null` or *empty* (for `String` and `Collection/Array`). 

The default strategy can be replaced by a custom implementation of `CriteriaStrategy` through `Sql.setCriteriaStrategy`, for a specific criteria, an implementation can always have three kinds of operation:
- return `null` - do not apply the criteria.
- return the passed in `strategy` parameter - apply the criteria directly. 
- return another **different/changed criteria** - apply a transformed/modified criteria.


## In-line Apply Strategy
A criteria or fragment invocation can be immediately followed by an in-line strategy, this gives more powerful controlling of the result sql, align the in-line strategies by using additional spaces can give a side-note style which will not break the fluency and clarity:

```java
Sql sql = new Sql()                              //@formatter:off
    .select("id")
    .from("A a")
    .leftJoin("B b On(a.refB=b.id)")             .$(Variables.isNotEmpty(form.b.prop))
    .leftJoin("C c On(b.refC=c.id)")             .$(joinC)
    .where()
      .eq("a.secret", someValue)                 .$(PrivilegeUtils.currentUserHasRole("query.by.secret")) 
      .eq("b.prop", form.b.prop)
    .endWhere();                                 //@formatter:on
```
The in-line strategy method `$(Boolean apply)` accept a Boolean parameter, when null or false, the fragment will not apply.  
**Note**:
- Each fragment/criteria invocation can followed by at most one `$` invocation.
- When pair(`grp&endGrp, subSql&endSubSql, in&endIn, ...`), `$` invocation can follow the begin or end method, but not both. 

## Criteria & Fragment methods
Copy from javadoc of `Sql`, `SqlD`, `CriteriaGroupD`, see javadoc for more descriptions.

**Custom-Fragment Methods** (SqlD)
- `__(String fragment, Object... bindVariables)` **add a custom cause.**
- `____(String fragment, Object... bindVariables)` add a custom item for ['Select'|'From'|'Order by'|'Group By'] previous cause.
 
**Simple-Criteria Methods Summary** 
- `eq(String, Object), ne(String, Object)`
- `gt(String, Object), lt(String, Object)`
- `ge(String, Object), le(String, Object)`
- `like(String, String), notLike(String, String)`
- `like(String, String, boolean, boolean), notLike(String, String, boolean, boolean)`
- `between(String, Object, Object), notBetween(String, Object, Object)`
- `in(String, Object), notIn(String, Object)`
- `isNull(String), isNotNull(String)`
- `__(String, Object...)` **add a custom criteria**.

**Sub-CriteriaGroup Methods Summary**
- `grp(), grp(boolean) | SubCriteriaGroup.endGrp()`

**Sub-Sql Methods Summary**
- `subSql(String) | SubSqlCriteria.endSubSql()`
- `exists() | ExistsSubSql.endExists()`
- `notExists() | NotExistsSubSql.endNotExists()`
- `in(String) | InSubSql.endIn()`
- `notIn(String) | NotInSubSql.endNotIn()`

The custom fragment/criteria methods can be used to join static string with zero or more bind-variables.


# Design Considerations
- All formal method are bind-variable based. for static fragments or criteria, please use custom method `__` or `____`.
- Criteria Group method (`Where(), grp()`) take *And Mode* by default, for *Or Mode* please use parameterlized form `Where(true), grp(true)`
- In-line apply strategy method `$(Boolean)` for complicated examine within context.  
- Strong typed with generic parameters for IDE tips.

This project was inspired by *groovy&grails* DSL/link style implementation at first.

# Usages

```java
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
		.endWhere()	.endSubSql()
	.endWhere().endSubSql()
.endWhere();//@formatter:on
Assert.assertEquals(
		"Select i From X x Where x.a=? And Exists(Select 1 From Y) And x.id in(Select z.id From Z z Where Exists(Select o.id From O o Where o.id=z.id))",
		sql.toString());
```