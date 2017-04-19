package org.jclouds.rest.annotations;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.http.HttpResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by battula on 18/04/17.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ResponseParserAsync {
    Class<? extends Function<ListenableFuture<HttpResponse>, ?>> value();
}
