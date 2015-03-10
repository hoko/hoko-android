package com.hoko.deeplinking.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on the fragment activities you wish to be deeplinkable.
 * <pre>{@code @DeeplinkFragment(id = R.id.content_view, fragments = {ProductFragment.class, CategoryFragment.class})
 * public class MainActivity extends FragmentActivity { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeeplinkFragment {

    public static final int noValue = -1;

    /**
     * The view identifier into which Hoko should add the fragment. (e.g. R.id.content_view)
     *
     * @return The view id.
     */
    public int id() default noValue;

    /**
     * An array of Fragment subclasses. These Fragment subclasses should be annotated with
     * DeeplinkRoute annotations. (e.g. {ProductFragment.class, CategoryFragment.class})
     *
     * @return An array of Fragment subclasses.
     */
    public Class[] fragments() default {};
}
