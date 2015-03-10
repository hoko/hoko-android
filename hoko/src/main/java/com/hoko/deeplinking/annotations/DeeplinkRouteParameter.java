package com.hoko.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the route parameter fields on your deeplinkable activities.
 * <pre>{@code @DeeplinkRouteParameter("product_id")
 * int productId;
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DeeplinkRouteParameter {
    String value();
}
