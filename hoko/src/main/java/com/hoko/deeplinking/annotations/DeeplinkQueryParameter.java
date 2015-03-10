package com.hoko.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the query parameter fields on your deeplinkable activities.
 * <pre>{@code @DeeplinkQueryParameter("language")
 * String languageCode;
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DeeplinkQueryParameter {
    String value();
}
