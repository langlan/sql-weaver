package langlan.sql.dsl.criteria.strategy;

import langlan.sql.dsl.criteria.*;
import langlan.sql.dsl.e.DevException;
import langlan.sql.dsl.i.Criteria;
import langlan.sql.dsl.i.CriteriaStrategy;
import langlan.sql.dsl.u.Variables;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultCriteriaStrategy implements CriteriaStrategy {
	public static final CriteriaStrategy INSTANCE = new DefaultCriteriaStrategy();
	private Map<Class<?>, Method> cache;

	public Criteria applyCriteria(Criteria criteria) {
		return criteria;
	}

	public Criteria applyCriteria(SingleValueNegativeTesting criteria) {
		AbstractSingleValueTestingCriteria interal = criteria.getInternal();
		AbstractSingleValueTestingCriteria interalApplied = (AbstractSingleValueTestingCriteria) apply(interal);
		if (interalApplied == null) {
			return null;
		} else if (interalApplied == interal) { // Not Transformed
			return criteria;
		} else { // Transformed
			return interalApplied.negative();
		}
	}

	public Criteria applyCriteria(BinaryComparison criteria) {
		return !Variables.isEmpty(criteria.getBoundValue()) ? criteria : null;
	}

	public Criteria applyCriteria(Between criteria) {
		boolean leftApply = Variables.isNotEmpty(criteria.getLeftBoundValue());
		boolean rightApply = Variables.isNotEmpty(criteria.getRightBoundValue());
		if (leftApply && rightApply) {
			return criteria;
		} else if (leftApply) {
			return new BinaryComparison(criteria.getTesting(), ">=", criteria.getLeftBoundValue());
		} else if (rightApply) {
			return new BinaryComparison(criteria.getTesting(), "<=", criteria.getLeftBoundValue());
		} else {
			return null;
		}
	}

	public Criteria applyCriteria(IsNull criteria) {
		return criteria;
	}

	public final Criteria apply(Criteria criteria) {
		Class<? extends Criteria> clazz = criteria.getClass();
		Method method = getStrategyMethod(clazz);
		try {
			return (Criteria) method.invoke(this, criteria);
		} catch (Exception e) {
			throw new DevException("Criteria-Strategy-Method Invoke Failed", e);
		}
	}

	public final Method getStrategyMethod(Class<? extends Criteria> criteriaType) {
		if (this.cache == null) { // load defined strategy methods
			synchronized (this) {
				if (this.cache == null) { // recheck
					HashMap<Class<?>, Method> temp = new HashMap<Class<?>, Method>();
					Method[] methods = this.getClass().getMethods();
					for (Method m : methods) {
						boolean b = Modifier.isPublic(m.getModifiers());
						b = b && m.getName().equals("applyCriteria");
						b = b && Criteria.class.isAssignableFrom(m.getReturnType());
						Class<?>[] ptypes = m.getParameterTypes();
						b = b && ptypes != null && ptypes.length == 1;
						b = b && Criteria.class.isAssignableFrom(ptypes[0]);
						if (b) {
							@SuppressWarnings("unchecked")
							Class<? extends Criteria> parmaterType = (Class<? extends Criteria>) ptypes[0];
							Method strategyMethod = temp.get(parmaterType);
							if (strategyMethod == null) {
								temp.put(parmaterType, m);
							} else if (strategyMethod.getDeclaringClass().isAssignableFrom(m.getDeclaringClass())) {
								temp.put(parmaterType, m);// override.
							}

						}
					}
					this.cache = Collections.unmodifiableMap(temp);
				}
			}
		}

		Method ret = cache.get(criteriaType);
		if (ret == null) {
			synchronized (this) {
				// recheck
				ret = cache.get(criteriaType);
				if (ret != null) {
					return ret;
				}

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

				// change cache
				Map<Class<?>, Method> temp = new HashMap<Class<?>, Method>();
				temp.put(criteriaType, ret);
				temp.putAll(cache);
				this.cache = Collections.unmodifiableMap(temp);
			}
		}
		return ret;
	}
}
