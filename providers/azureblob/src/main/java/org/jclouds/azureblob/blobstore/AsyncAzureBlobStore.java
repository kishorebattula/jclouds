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

package org.jclouds.azureblob.blobstore;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.functions.AzureBlobToBlob;
import org.jclouds.azureblob.blobstore.functions.AzureListBlobBlocksResponseToMultipartList;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.azureblob.blobstore.functions.CreateContainerOptionsToAzureCreateContainerOptions;
import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.azureblob.domain.ListBlobBlocksResponse;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.functions.BlobToHttpGetOptions;
import org.jclouds.blobstore.internal.AsyncBaseBlobStore;
import org.jclouds.blobstore.options.CreateContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.domain.Location;
import org.jclouds.http.options.GetOptions;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;

import java.util.List;

public class AsyncAzureBlobStore extends AsyncBaseBlobStore {
    private final AzureBlobClient client;
    private final BlobToAzureBlob blob2AzureBlob;
    private final AzureBlobToBlob azureBlob2Blob;
    private final BlobToHttpGetOptions blob2ObjectGetOptions;
    private final CreateContainerOptionsToAzureCreateContainerOptions createContainerOptionsToAzureContainerOptions;
    private final AzureListBlobBlocksResponseToMultipartList azureListBlobBlocksResponseToMultipartList;

    @Inject
    AsyncAzureBlobStore(BlobStoreContext context, BlobUtils blobUtils, AzureBlobClient client,
       BlobToAzureBlob blob2AzureBlob, AzureBlobToBlob azureBlob2Blob, BlobToHttpGetOptions blob2ObjectGetOptions,
       PayloadSlicer slicer,
       CreateContainerOptionsToAzureCreateContainerOptions createContainerOptionsToAzureContainerOptions,
       AzureListBlobBlocksResponseToMultipartList azureListBlobBlocksResponseToMultipartList) {
        super(context, blobUtils, slicer);
        this.client = client;
        this.blob2AzureBlob = blob2AzureBlob;
        this.azureBlob2Blob = azureBlob2Blob;
        this.blob2ObjectGetOptions = blob2ObjectGetOptions;
        this.createContainerOptionsToAzureContainerOptions = createContainerOptionsToAzureContainerOptions;
        this.azureListBlobBlocksResponseToMultipartList = azureListBlobBlocksResponseToMultipartList;
    }

    /**
     * This implementation invokes {@link AzureBlobClient#containerExistsAsync(String)}
     * @param container
     *          container name
     */
    @Override
    public ListenableFuture<Boolean> containerExists(String container) {
        return client.containerExistsAsync(container);
    }

    /**
     * This implementation invokes {@link AzureBlobClient#createContainer}
     *
     * @param location
     *           currently ignored
     * @param container
     *           container name
     */
    @Override
    public ListenableFuture<Boolean> createContainerInLocation(Location location, String container) {
        return client.createContainerAsync(container);
    }

    @Override
    public ListenableFuture<Boolean> createContainerInLocation(Location location, String container,
       CreateContainerOptions options) {
       return client.createContainerAsync(container, createContainerOptionsToAzureContainerOptions.apply(options));
    }

    /**
     * This implementation invokes {@link AzureBlobClient#blobExistsAsync}
     *
     * @param container
     *           container name
     * @param name
     *           blob name
     */
    @Override
    public ListenableFuture<Boolean> blobExists(String container, String name) {
        return client.blobExistsAsync(container, name);
    }

    @Override
    public ListenableFuture<String> putBlob(String container, Blob blob) {
        return client.putBlobAsync(container, blob2AzureBlob.apply(blob));
    }

    @Override
    public ListenableFuture<String> putBlob(String container, Blob blob, PutOptions options) {
        if (options.getBlobAccess() != BlobAccess.PRIVATE) {
            throw new UnsupportedOperationException("blob access not supported by Azure");
        }

        if (options.isMultipart()) {
            return putMultipartBlob(container, blob, options);
        }

        return putBlob(container, blob);
    }

    @Override
    public ListenableFuture<Blob> getBlob(String container, String key,
        org.jclouds.blobstore.options.GetOptions options) {
        GetOptions azureOptions = blob2ObjectGetOptions.apply(options);
        return Futures.transform(client.getBlobAsync(container, key, azureOptions), azureBlob2Blob);
    }

    @Override
    public ListenableFuture<MultipartUpload> initiateMultipartUpload(String container, BlobMetadata blobMetadata,
        PutOptions options) {
        return Futures.immediateFuture(AzureBlobStoreCommon.initiateMultipartUpload(container, blobMetadata, options));
    }

    @Override
    public ListenableFuture<Void> abortMultipartUpload(MultipartUpload mpu) {
        // Azure automatically removes uncommitted blocks after 7 days:
        // http://gauravmantri.com/2012/05/11/comparing-windows-azure-blob-storage-and-amazon-simple-storage-service-s3part-ii/#f020
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<String> completeMultipartUpload(MultipartUpload mpu, List<MultipartPart> parts) {
        AzureBlob azureBlob = client.newBlob();
        azureBlob = AzureBlobStoreCommon.prepareDummyBlobForMultipartComplete(mpu, azureBlob);
        List<String> blocks = AzureBlobStoreCommon.prepareBlockList(parts);
        return client.putBlockListAsync(mpu.containerName(), azureBlob, blocks);
    }

    @Override
    public ListenableFuture<MultipartPart> uploadMultipartPart(MultipartUpload mpu, int partNumber, Payload payload) {
        String blockId = AzureBlobStoreCommon.prepareBlockId(partNumber);
        client.putBlock(mpu.containerName(), mpu.blobName(), blockId, payload);
        return Futures.immediateFuture(AzureBlobStoreCommon.prepareMultipartResponse(partNumber));
    }

    @Override
    public ListenableFuture<List<MultipartPart>> listMultipartUpload(MultipartUpload mpu) {
        ListenableFuture<ListBlobBlocksResponse> response;
        try {
            response = client.getBlockListAsync(mpu.containerName(), mpu.blobName());
        } catch (KeyNotFoundException knfe) {
            SettableFuture<List<MultipartPart>> partsListFuture = SettableFuture.create();
            partsListFuture.set(ImmutableList.<MultipartPart>of());
            return partsListFuture;
        }

        return Futures.transform(response, azureListBlobBlocksResponseToMultipartList);
    }

    @Override
    public long getMinimumMultipartPartSize() {
        return AzureBlobStoreCommon.MINIMUM_MULTIPART_SIZE;
    }

    @Override
    public long getMaximumMultipartPartSize() {
        return AzureBlobStoreCommon.MAXIMUM_MULTIPART_SIZE;
    }

    @Override
    public int getMaximumNumberOfParts() {
        return AzureBlobStoreCommon.MAXIMUM_NUMBER_OF_PARTS;
    }
}
