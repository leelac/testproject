package uk.co.britishgas.redis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Identifies a parameter to be sent as a JSON list of properties. Each property
 * of JSON Object can be mapped to an Object.
 * </p>
 * <b>Usage:</b>
 * <p>
 * Following JSON notation in a POST can be targeted to specific objects as
 * highlighted. <code><pre>
 * {
 * &nbsp;&nbsp;&nbsp;&nbsp;"order":{...}
 * &nbsp;&nbsp;&nbsp;&nbsp;"meta":{...}
 * }
 * </pre></code>
 * </p>
 * <p>
 * <code><pre>
 * &#064;POST("/shop/order")
 * &#064;Consumes(MediaType.APPLICATION_JSON)
 * void createOrder(<b>@JsonParam("order")</b> Order order, <b>@JsonParam("meta")</b> Meta meta);</pre>
 * </code>
 * </p>
 * <br/>
 * <br/>
 * 
 * @author Pradeep Krishna Govindaraju (govindp1)
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonParam {

    /**
     * <p>
     * The name of the JSON parameter which identifies the object to be
     * constructed.
     * </p>
     * 
     * @return the name of the JSON parameter
     */
    String value();

    boolean isCollection() default false;

    Class<?> type() default Object.class;
}