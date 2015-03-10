package com.hoko.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on one activity you wish to be default deeplinkable activity.
 * <pre>{@code @DeeplinkDefaultRoute
 * public class SplashActivity extends Activity { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeeplinkDefaultRoute {
}