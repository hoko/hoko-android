package com.hokolinks.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on one activity or fragment's property you wish to receive metadata.
 * <pre>{@code @DeeplinkMetadata
 * JSONObject metadata;
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DeeplinkMetadata {
}