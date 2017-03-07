package org.jclouds.rest.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by sthakur on 06/03/17.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Async {
}
