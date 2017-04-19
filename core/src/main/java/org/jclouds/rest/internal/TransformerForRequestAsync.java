package org.jclouds.rest.internal;

import com.google.common.base.Function;
import com.google.common.reflect.Invokable;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.reflect.Invocation;
import org.jclouds.rest.annotations.ResponseParserAsync;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by battula on 19/04/17.
 */
public class TransformerForRequestAsync implements Function<HttpRequest, Function<ListenableFuture<HttpResponse>, ?>> {
    private final Injector injector;

    @Inject
    TransformerForRequestAsync(Injector injector) {
        this.injector = injector;
    }

    @Override
    public Function<ListenableFuture<HttpResponse>, ?> apply(final HttpRequest input) {
        GeneratedHttpRequest request = GeneratedHttpRequest.class.cast(input);
        return getTransformerForMethod(request.getInvocation(), injector);
    }

    private Function<ListenableFuture<HttpResponse>, ?> getTransformerForMethod(Invocation invocation, Injector injector) {
        return injector.getInstance(getParserOrThrowException(invocation));
    }

    private Key<? extends Function<ListenableFuture<HttpResponse>, ?>> getParserOrThrowException(Invocation invocation) {
        Invokable<?, ?> invoked = invocation.getInvokable();
        ResponseParserAsync annotation = invoked.getAnnotation(ResponseParserAsync.class);
        if (annotation == null) {
            throw new IllegalStateException("You must specify a ResponseParser annotation on: " + invoked.toString());
        }

        return Key.get(annotation.value());
    }
}
