/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jclouds.blobstore.internal;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.strategy.internal.MultipartUploadSlicingAlgorithm;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.blobstore.util.internal.BlobUtilsImpl;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncBaseBlobStore extends AbstractBlobStore implements AsyncBlobStore {

    @Inject
    protected AsyncBaseBlobStore(BlobStoreContext context, BlobUtils blobUtils, PayloadSlicer slicer) {
        super(context, blobUtils, slicer);
    }

    @Override
    public BlobStoreContext getContext() {
        return context;
    }

    /**
     * invokes {@link BlobUtilsImpl#blobBuilder }
     */
    @Override
    public BlobBuilder blobBuilder(String name) {
        return blobUtils.blobBuilder().name(name);
    }

    /**
     * This implementation invokes
     * {@link #getBlob(String,String,org.jclouds.blobstore.options.GetOptions)}
     *
     * @param container
     *           container name
     * @param key
     *           blob key
     */
    @Override
    public ListenableFuture<Blob> getBlob(String container, String key) {
        return getBlob(container, key, org.jclouds.blobstore.options.GetOptions.NONE);
    }

    @Beta
    protected ListenableFuture<String> putMultipartBlob(final String container, final Blob blob,
          final PutOptions overrides) {
        ArrayList<ListenableFuture<MultipartPart>> parts = new ArrayList<ListenableFuture<MultipartPart>>();
        final ListenableFuture<MultipartUpload> mpu = initiateMultipartUpload(container, blob.getMetadata(), overrides);
        return Futures.transform(mpu, new AsyncFunction<MultipartUpload, String>() {
            @Override
            public ListenableFuture<String> apply(final MultipartUpload mpu) {
                long contentLength = blob.getMetadata().getContentMetadata().getContentLength();
                MultipartUploadSlicingAlgorithm algorithm = new MultipartUploadSlicingAlgorithm(
                        getMinimumMultipartPartSize(), getMaximumMultipartPartSize(), getMaximumNumberOfParts());
                long partSize = algorithm.calculateChunkSize(contentLength);
                int partNumber = 1;
                List<ListenableFuture<MultipartPart>> futures = new ArrayList<ListenableFuture<MultipartPart>>();
                for (Payload payload : slicer.slice(blob.getPayload(), partSize)) {
                    futures.add(uploadMultipartPart(mpu, partNumber++, payload));
                }
                final SettableFuture<String> finalFuture = SettableFuture.create();
                Futures.addCallback(Futures.allAsList(futures), new FutureCallback<List<MultipartPart>>() {
                    @Override
                    public void onSuccess(List<MultipartPart> savedParts) {
                        Futures.transform(completeMultipartUpload(mpu, savedParts), new Function<String, Void>() {
                            @Override
                            public Void apply(String input) {
                                finalFuture.set(input);
                                return null;
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        abortMultipartUpload(mpu);
                        finalFuture.setException(t);
                    }
                });
                return finalFuture;
            }
        });
    }
}
