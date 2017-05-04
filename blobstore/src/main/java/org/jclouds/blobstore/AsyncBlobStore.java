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

package org.jclouds.blobstore;

import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.javax.annotation.Nullable;

/**
 * Asynchronous access to BlobStore like Amazon s3 and Azure storage etc...
 */
public interface AsyncBlobStore {
    /**
     * @return a reference to the context that created this BlobStore.
     */
    BlobStoreContext getContext();

    /**
     *
     * @return builder for creating new {@link Blob}s
     */
    BlobBuilder blobBuilder(String name);

    /**
     * Adds a {@code Blob} representing the data at location {@code container/blob.metadata.name}
     *
     * @param container
     *           container to place the blob.
     * @param blob
     *           fully qualified name relative to the container.
     * @return listenable future which returns etag of the blob you uploaded, possibly null where etags are unsupported
     */
    ListenableFuture<String> putBlob(String container, Blob blob);

    /**
     * Retrieves a {@code Blob} representing the data at location {@code container/name}
     *
     * @param container
     *           container where this exists.
     * @param name
     *           fully qualified name relative to the container.
     * @return listenable future which contains the blob you intended to receive or null, if it doesn't exist.
     * @throws ContainerNotFoundException
     *            if the container doesn't exist
     */
    @Nullable
    ListenableFuture<Blob> getBlob(String container, String name);

    /**
     * Retrieves a {@code Blob} representing the data at location {@code container/name}
     *
     * @param container
     *           container where this exists.
     * @param name
     *           fully qualified name relative to the container.
     * @param options
     *           byte range or condition options
     * @return listenable future which contains the blob you intended to receive or null, if it doesn't exist.
     * @throws ContainerNotFoundException
     *            if the container doesn't exist
     */
    @Nullable
    ListenableFuture<Blob> getBlob(String container, String name, GetOptions options);
}
