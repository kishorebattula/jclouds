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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.blobstore.util.internal.BlobUtilsImpl;

public abstract class AsyncBaseBlobStore extends AbstractBlobStore implements AsyncBlobStore {

    @Inject
    protected AsyncBaseBlobStore(BlobStoreContext context, BlobUtils blobUtils) {
        super(context, blobUtils);
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
}
