/**************************************************************************
 *                                                                        *
 * ADOBE CONFIDENTIAL                                                     *
 * ___________________                                                    *
 *                                                                        *
 *  Copyright 2017 Adobe Systems Incorporated                             *
 *  All Rights Reserved.                                                  *
 *                                                                        *
 * NOTICE:  All information contained herein is, and remains              *
 * the property of Adobe Systems Incorporated and its suppliers,          *
 * if any.  The intellectual and technical concepts contained             *
 * herein are proprietary to Adobe Systems Incorporated and its           *
 * suppliers and are protected by trade secret or copyright law.          *
 * Dissemination of this information or reproduction of this material     *
 * is strictly forbidden unless prior written permission is obtained      *
 * from Adobe Systems Incorporated.                                       *
 **************************************************************************/

package org.jclouds.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by sthakur on 06/03/17.
 */
public class HttpRequestExecutor {


  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestExecutor.class);
  // ToDo make these configurable
  private static final int MAX_TOTAL_CONNECTIONS = 100;
  private static final int MAX_CONNECTIONS_PER_ROUTE = 100;

  private CloseableHttpAsyncClient httpAsyncClient;

  public HttpRequestExecutor() {
    final HttpAsyncClientBuilder httpAsyncClientBuilder = HttpAsyncClients.custom();
    httpAsyncClientBuilder.setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
        .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE);

    httpAsyncClient = httpAsyncClientBuilder.build();
    httpAsyncClient.start();
    System.out.println("STARTING ASYNC LIBRARY");
  }

  /**
   * This method is use to copy the mdc context to current thread.
   *
   * @param mdcCopy copy of mdc context
   */
  private void setMDCContext(final Map<String, String> mdcCopy) {
    if (mdcCopy != null) {
      MDC.setContextMap(mdcCopy);
    }
  }

  /**
   * executes the async post request and returns future of response
   *
   * @param httpRequest Http request to execute
   * @return future of httpResponse
   */
  public CompletableFuture<HttpResponse> executeHttpRequest(final HttpRequestBase httpRequest) {
    LOGGER.debug("Sending async request");

    final Map<String, String> mdcCopy = MDC.getCopyOfContextMap();

    final CompletableFuture<HttpResponse> future = new CompletableFuture<HttpResponse>();
    httpAsyncClient.execute(httpRequest, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(final HttpResponse httpResponse) {
        HttpRequestExecutor.this.setMDCContext(mdcCopy);
        LOGGER.debug("Request completed");
        future.complete(httpResponse);
      }

      @Override
      public void failed(final Exception e) {
        HttpRequestExecutor.this.setMDCContext(mdcCopy);
        LOGGER.debug("Request failed", e);
        future.completeExceptionally(new Exception("FAILED REQUEST"));
      }

      @Override
      public void cancelled() {
        HttpRequestExecutor.this.setMDCContext(mdcCopy);
        LOGGER.debug("Request cancelled");
        future.completeExceptionally(new Exception("CANCELLED REQUEST"));
      }
    });
    return future;
  }
}
