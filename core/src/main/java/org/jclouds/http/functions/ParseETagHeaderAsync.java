package org.jclouds.http.functions;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.http.HttpResponse;

import javax.inject.Singleton;

/**
 * Created by battula on 18/04/17.
 */
@Singleton
public class ParseETagHeaderAsync implements Function<ListenableFuture<HttpResponse>, ListenableFuture<String>> {

    @Override
    public ListenableFuture<String> apply(final ListenableFuture<HttpResponse> future) {
        return Futures.transform(future, new Function<HttpResponse, String>() {
            @Override
            public String apply(final HttpResponse input) {
                return new ParseETagHeader().apply(input);
            }
        });
    }
}
