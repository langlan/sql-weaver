package langlan.sql.weaver.u.form;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.Assert;

/**
 * A Spring-Converter Helping data-bind for Range. <p>
 * 
 * Usage Example With Spring MVC and a <i>Dao</i>:
 * 
 * <pre>
 * <code>
 * // register range-converter.
 * &#64;Configuration
 * public class WebConfig implements WebMvcConfigurer {
 *     &#64;Override
 *     public void addFormatters(FormatterRegistry registry) {
 *         registry.addConverter(new StringToRange((ConversionService) registry));
 *     }
 * }
 * 
 * // declare a form containing one or more Range fields.
 * public class EmployeeSearchParams {
 *     public Range<String> alphabetic;
 *     public Range<Integer> salary;
 *     // other fields and setters.
 * }
 * 
 * 
 * // controller taking the form to driven Spring-MVC data-binding for form and specifically it's range fields.
 * // e.g. GET /employee?salary=(1000, 2000]&alphabetic=[a, b] 
 * &#64;RestController
 * &#64;ResquestMapping("/employee")
 * public class EmployeeController {
 *     private @Autowrired EmployeeService service;
 *     &#64;GetMapping
 *     public List<Emplyoee> search(EmployeeSearchParams params){// Or simply Range<Type> parameter.
 *          return service.search(params);
 *     }
 * }
 * 
 * // Service: build sql/hql/jpql and do query.
 * &#64;Service
 * public class EmployeeService {
 *     private &#64;Autowrired Dao dao;
 *     &#64;Transactional
 *     public List<Emplyoee> search(EmployeeSearchParams params){
 *         Sql ql = new Sql().select("m").from("Emplyoee m").where() //@formatter:off
 *             .grp(true)
 *                 .like("m.name", params.q, true, true)
 *                 .like("m.shortName", params.q, true, true)
 *             .endGrp()
 *             .between("m.salary", params.salary)
 *             .between("m.alphabetic", params.alphabetic)
 *             // ... other criteria.
 *         .endWhere(); //@formatter:on
 *         return dao.find(ql.toString(), ql.vars());
 *     }
 * }
 * 
 * </code>
 * </pre>
 * 
 *
 */
public class StringToRangeConverter implements ConditionalGenericConverter {
	private final ConversionService conversionService;

	public StringToRangeConverter(ConversionService conversionService) {
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.conversionService = conversionService;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Range.class));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		Class<?> elementClass = targetType.getResolvableType().getGeneric(0).resolve();
		Range<String> stringRange = Range.of((String) source);

		if (elementClass.isAssignableFrom(String.class)) {
			return stringRange;
		}

		TypeDescriptor targetElType = TypeDescriptor.valueOf(elementClass);

		Object min = this.conversionService.convert(stringRange.getMin(), sourceType, targetElType);
		Object max = this.conversionService.convert(stringRange.getMax(), sourceType, targetElType);
		return Range.of(min, max, stringRange.isMinExclusive(), stringRange.isMaxExclusive(), stringRange.getRaw());

	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		Class<?> elementClass = targetType.getResolvableType().getGeneric(0).resolve();
		return this.conversionService.canConvert(sourceType, TypeDescriptor.valueOf(elementClass));
	}
}