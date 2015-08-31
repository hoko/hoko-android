package com.hokolinks.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the activities you wish to be deeplinkable.
 * <pre>{@code @DeeplinkMultipleRoute(routes = {"product/:product_id", "product/:product_id/:gender"})
 * public class ProductActivity extends Activity { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeeplinkMultipleRoute {

    String[] routes() default {};

}
