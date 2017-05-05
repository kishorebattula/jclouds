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

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.GetOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.domain.Location;
import org.jclouds.io.Payload;
import org.jclouds.javax.annotation.Nullable;

import java.util.List;

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
     * determines if a service-level container exists
     */
    ListenableFuture<Boolean> containerExists(String container);

    /**
     * Creates a namespace for your blobs
     * <p/>
     *
     * A container is a namespace for your objects. Depending on the service, the scope can be
     * global, identity, or sub-identity scoped. For example, in Amazon S3, containers are called
     * buckets, and they must be uniquely named such that no-one else in the world conflicts. In
     * other blobstores, the naming convention of the container is less strict. All blobstores allow
     * you to list your containers and also the contents within them. These contents can either be
     * blobs, folders, or virtual paths.
     *
     * @param location
     *           some blobstores allow you to specify a location, such as US-EAST, for where this
     *           container will exist. null will choose a default location
     * @param container
     *           namespace. Typically constrained to lowercase alpha-numeric and hyphens.
     * @return true if the container was created, false if it already existed.
     */
    ListenableFuture<Boolean> createContainerInLocation(@Nullable Location location, String container);

    /**
     *
     * @param options
     *           controls default access control
     * @see #createContainerInLocation(Location,String)
     */
    ListenableFuture<Boolean> createContainerInLocation(@Nullable Location location, String container,
          CreateContainerOptions options);

    /**
     * Determines if a blob exists
     *
     * @param container
     *           container where the blob resides
     * @param name
     *           full path to the blob
     */
    ListenableFuture<Boolean> blobExists(String container, String name);

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
     * Adds a {@code Blob} representing the data at location {@code container/blob.metadata.name}
     * options using multipart strategies.
     *
     * @param container
     *           container to place the blob.
     * @param blob
     *           fully qualified name relative to the container.
     * @param options
     *           byte range options
     * @return etag of the blob you uploaded, possibly null where etags are unsupported
     * @throws ContainerNotFoundException
     *            if the container doesn't exist
     */
    ListenableFuture<String> putBlob(String container, Blob blob, PutOptions options);

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

    @Beta
    ListenableFuture<MultipartUpload> initiateMultipartUpload(String container, BlobMetadata blob, PutOptions options);

    @Beta
        // TODO: take parts?
    ListenableFuture<Void> abortMultipartUpload(MultipartUpload mpu);

    @Beta
    ListenableFuture<String> completeMultipartUpload(MultipartUpload mpu, List<MultipartPart> parts);

    @Beta
    ListenableFuture<MultipartPart> uploadMultipartPart(MultipartUpload mpu, int partNumber, Payload payload);

    @Beta
    ListenableFuture<List<MultipartPart>> listMultipartUpload(MultipartUpload mpu);

    @Beta
    long getMinimumMultipartPartSize();

    @Beta
    long getMaximumMultipartPartSize();

    @Beta
    int getMaximumNumberOfParts();
}
