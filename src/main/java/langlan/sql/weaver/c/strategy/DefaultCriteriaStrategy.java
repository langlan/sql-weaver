package langlan.sql.weaver.c.strategy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import langlan.sql.weaver.c.Between;
import langlan.sql.weaver.c.BetweenRange;
import langlan.sql.weaver.c.BinaryComparison;
import langlan.sql.weaver.c.Custom;
import langlan.sql.weaver.c.IsNull;
import langlan.sql.weaver.e.DevException;
import langlan.sql.weaver.i.Criteria;
import langlan.sql.weaver.i.CriteriaStrategy;
import langlan.sql.weaver.u.Variables;
import langlan.sql.weaver.u.form.Range;

/**
 * A default {@link CriteriaStrategy} implementation. <p> This class implements several methods each responsible for a
 * specific Criteria class. Subclasses can override or define other
 * 
 * <pre>
 *     <code><span style="font-weight: bold">public</span> Criteria applyCriteria(Criteria_Specific_Type)</code>
 * </pre>
 * 
 * stategy-methods to support custom logic. <p> NOTE: stategy-method <ul> <li>returns null means/indicate criteria
 * should not apply</li> <li>returns original-criteria means/indicate criteria should apply straightforward</li>
 * <li>returns other-not-null-criteria means/indicate should apply returned criteria instead of the original</li> </ul>
 *
 * @see #applyCriteria(Between)
 * @see #applyCriteria(BinaryComparison)
 * @see #applyCriteria(IsNull)
 * @see #applyCriteria(Criteria)
 */
public class DefaultCriteriaStrategy implements CriteriaStrategy {
	public static final CriteriaStrategy INSTANCE = new DefaultCriteriaStrategy();
	public static final String TIP_IS_NULL = "__IS__NULL__";
	private Map<Class<? extends Criteria>, Method> cache = initCache();
	
	protected DefaultCriteriaStrategy() {
		// TODO Auto-generated constructor stub
	}

	/** default Strategy method : this implementation just return original not matter what */
	public Criteria applyCriteria(Criteria criteria) {
		return criteria;
	}

	/**
	 * Strategy method for {@link BinaryComparison} <ul> <li>bound-value not empty: original</li> <li>bound-value empty:
	 * null(not apply)</li> </ul>
	 */
	public Criteria applyCriteria(BinaryComparison criteria) {
		return Variables.isNotEmpty(criteria.getBoundValue()) ? criteria : null;
	}

	/**
	 * Strategy method for {@link Between} <ul> <li>both left, right not null and not equals: -> original</li> <li>both
	 * left, right not null and equals: -> prop=left</li> <li>only left not null: -> prop>=?(left)</li> <li>only right
	 * not null: -> prop<=?(right)</li> <li>both left, right null: -> null(not apply)</li> </ul>
	 */
	public Criteria applyCriteria(Between criteria) {
		boolean leftApply = Variables.isNotEmpty(criteria.getLeftBoundValue());
		boolean rightApply = Variables.isNotEmpty(criteria.getRightBoundValue());
		if (leftApply && rightApply) {
			if (Variables.equals(criteria.getLeftBoundValue(), criteria.getRightBoundValue())) {
				String operator = criteria.isNegative() ? "<>" : "=";
				return new BinaryComparison(criteria.getTesting(), operator, criteria.getLeftBoundValue());
			} else {
				return criteria;
			}
		} else if (leftApply) {
			String operator = criteria.isNegative() ? "<" : ">=";
			return new BinaryComparison(criteria.getTesting(), operator, criteria.getLeftBoundValue());
		} else if (rightApply) {
			String operator = criteria.isNegative() ? ">" : "<=";
			return new BinaryComparison(criteria.getTesting(), operator, criteria.getRightBoundValue());
		} else {
			return null;
		}
	}

	/**
	 * <ul> <li>both left, right not null and not equals: -> original</li> <li>both left, right not null and equals: ->
	 * prop=left</li> <li>only left not null: -> prop>=?(left)</li> <li>only right not null: -> prop<=?(right)</li>
	 * <li>both left, right null: -> null(not apply)</li> </ul>
	 */
	public Criteria applyCriteria(BetweenRange criteria) {
		Range<?> range = criteria.getRange();
		if (range == null) {
			return null;
		} else if (TIP_IS_NULL.equals(range.getRaw())) {
			return new IsNull(criteria.getTesting());
		}

		String prop = criteria.getTesting();
		Object min = range.getMin(), max = range.getMax();
		boolean minApply = Variables.isNotEmpty(min), maxApply = Variables.isNotEmpty(max);
		boolean minEx = range.isMinExclusive(), maxEx = range.isMaxExclusive();
		boolean negative = criteria.isNegative();

		if ((!minEx || !minApply) && (!maxEx || !maxApply)) { // if no applicable-exclusive
			return applyCriteria((Between) criteria);
		} else if (minApply && maxApply) { // both apply
			if (Variables.equals(min, max)) {// TODO: may be we need a configuration point?
				if (minEx && maxEx) { // both exclusive
					return !negative ? new Custom("1<>1") : new Custom("1=1");
				} else { // foo between (x, x] === foo=x
					return new BinaryComparison(criteria.getTesting(), !negative ? "=" : "<>", min);
				}
			}
			return criteria;
		}
		// half-apply (and exclusive)
		if (minApply) {
			return new BinaryComparison(prop, negative ? ">" : "<=", min);
		} else { // maxApply
			return new BinaryComparison(prop, negative ? "<" : ">=", max);
		}
	}

	/** Strategy method for {@link IsNull} */
	public Criteria applyCriteria(IsNull criteria) {
		return criteria;
	}

	/** Dispatcher of strategy methods */
	public final Criteria apply(Criteria criteria) {
		Class<? extends Criteria> clazz = criteria.getClass();
		Method method = getStrategyMethod(clazz);
		method.setAccessible(true);
		try {
			return (Criteria) method.invoke(this, criteria);
		} catch (Exception e) {
			throw new DevException("Criteria-Strategy-Method Invoke Failed", e);
		}
	}

	// find all declared strategy-methods and put them in cache.
	private Map<Class<? extends Criteria>, Method> initCache() {
		ConcurrentHashMap<Class<? extends Criteria>, Method> map = new ConcurrentHashMap<Class<? extends Criteria>, Method>();
		Method[] methods = this.getClass().getMethods();
		for (Method m : methods) {
			// convention : should be public
			boolean b = Modifier.isPublic(m.getModifiers());
			// convention : name should be `applyCriteria`
			b = b && m.getName().equals("applyCriteria");
			// convention : return type should be (or sub-type of) Criteria
			b = b && Criteria.class.isAssignableFrom(m.getReturnType());
			Class<?>[] ptypes = m.getParameterTypes();
			b = b && ptypes != null && ptypes.length == 1;
			// convention: should take exactly-one parameter whose type should be (or sub-type of) Criteria
			b = b && Criteria.class.isAssignableFrom(ptypes[0]);
			if (b) {
				@SuppressWarnings("unchecked")
				Class<? extends Criteria> parameterType = (Class<? extends Criteria>) ptypes[0];
				Method existsM = map.get(parameterType);
				if (existsM == null) {
					map.put(parameterType, m);
				} else if (existsM.getDeclaringClass().isAssignableFrom(m.getDeclaringClass())) {
					map.put(parameterType, m);// override.
				}
			}
		}
		return map;
	}

	/** lookup a method for a criteria type */
	public final Method getStrategyMethod(Class<? extends Criteria> criteriaType) {
		Method ret = cache.get(criteriaType);
		if (ret == null) {
			// find strategy
			Class<?> superTypeLoop = criteriaType;
			while (ret == null && superTypeLoop != null && Criteria.class.isAssignableFrom(superTypeLoop)) {
				ret = cache.get(superTypeLoop);
				if (ret == null) {// by interfaces
					Class<?>[] interfaces = superTypeLoop.getInterfaces();
					Class<?> matched = null;
					for (Class<?> i : interfaces) {
						if (cache.get(i) != null) {
							if (matched == null || matched.isAssignableFrom(i)) {
								matched = i;
							}
						}
					}
					if (matched != null) {
						ret = cache.get(matched);
					}
				}
				superTypeLoop = superTypeLoop.getSuperclass();
			}

			cache.putIfAbsent(criteriaType, ret);
		}
		return ret;
	}
}
