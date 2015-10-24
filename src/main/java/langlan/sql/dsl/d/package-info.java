/**
 * This package exits for supporting both inheritable and especially return instance of [concrete derived type/scoped
 * type] by methods.
 * Since standard Java grammar dose not support <ul>
 * <li>return <code>this</code> as the concrete type in methods of supper class</li>
 * <li>return [nested/parent] scope/context in same method</li>
 * </ul>
 * by simply syntax. So this package leverage Generic Type Paramenters to achieve those goals, even though the codes
 * seemed more verbose.
 */
package langlan.sql.dsl.d;