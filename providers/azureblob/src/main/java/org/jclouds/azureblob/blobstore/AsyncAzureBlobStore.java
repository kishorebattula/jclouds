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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.functions.AzureBlobToBlob;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.azureblob.blobstore.functions.CreateContainerOptionsToAzureCreateContainerOptions;
import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.azureblob.domain.BlobBlockProperties;
import org.jclouds.azureblob.domain.ListBlobBlocksResponse;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
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
import org.jclouds.io.MutableContentMetadata;
import org.jclouds.io.Payload;

import java.util.List;
import java.util.UUID;

/**
 * Created by battula on 25/04/17.
 */
public class AsyncAzureBlobStore extends AsyncBaseBlobStore {
    private final AzureBlobClient client;
    private final BlobToAzureBlob blob2AzureBlob;
    private final AzureBlobToBlob azureBlob2Blob;
    private final BlobToHttpGetOptions blob2ObjectGetOptions;
    private final CreateContainerOptionsToAzureCreateContainerOptions createContainerOptionsToAzureContainerOptions;

    @Inject
    AsyncAzureBlobStore(BlobStoreContext context, BlobUtils blobUtils, AzureBlobClient client,
       BlobToAzureBlob blob2AzureBlob, AzureBlobToBlob azureBlob2Blob, BlobToHttpGetOptions blob2ObjectGetOptions,
       CreateContainerOptionsToAzureCreateContainerOptions createContainerOptionsToAzureContainerOptions) {
        super(context, blobUtils);
        this.client = client;
        this.blob2AzureBlob = blob2AzureBlob;
        this.azureBlob2Blob = azureBlob2Blob;
        this.blob2ObjectGetOptions = blob2ObjectGetOptions;
        this.createContainerOptionsToAzureContainerOptions = createContainerOptionsToAzureContainerOptions;
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

    @Override
    public ListenableFuture<String> putBlob(String container, Blob blob) {
        return client.putBlobAsync(container, blob2AzureBlob.apply(blob));
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
        String uploadId = UUID.randomUUID().toString();
        SettableFuture<MultipartUpload> response = SettableFuture.create();
        response.set(MultipartUpload.create(container, blobMetadata.getName(), uploadId, blobMetadata, options));
        return response;
    }

    @Override
    public ListenableFuture<Void> abortMultipartUpload(MultipartUpload mpu) {
        // Azure automatically removes uncommitted blocks after 7 days:
        // http://gauravmantri.com/2012/05/11/comparing-windows-azure-blob-storage-and-amazon-simple-storage-service-s3part-ii/#f020
        SettableFuture<Void> response = SettableFuture.create();
        response.set(null);
        return response;
    }

    @Override
    public ListenableFuture<String> completeMultipartUpload(MultipartUpload mpu, List<MultipartPart> parts) {
        AzureBlob azureBlob = client.newBlob();

        // fake values to satisfy BindAzureBlobMetadataToMultipartRequest
        azureBlob.setPayload(new byte[0]);
        azureBlob.getProperties().setContainer(mpu.containerName());
        azureBlob.getProperties().setName(mpu.blobName());

        azureBlob.getProperties().setContentMetadata((MutableContentMetadata) mpu.blobMetadata().getContentMetadata());
        azureBlob.getProperties().setMetadata(mpu.blobMetadata().getUserMetadata());

        ImmutableList.Builder<String> blocks = ImmutableList.builder();
        for (MultipartPart part : parts) {
            String blockId = BaseEncoding.base64().encode(Ints.toByteArray(part.partNumber()));
            blocks.add(blockId);
        }
        return client.putBlockListAsync(mpu.containerName(), azureBlob, blocks.build());
    }

    @Override
    public ListenableFuture<MultipartPart> uploadMultipartPart(MultipartUpload mpu, int partNumber, Payload payload) {
        String blockId = BaseEncoding.base64().encode(Ints.toByteArray(partNumber));
        client.putBlock(mpu.containerName(), mpu.blobName(), blockId, payload);
        String eTag = "";  // putBlock does not return ETag
        long partSize = -1;  // TODO: how to get this from payload?
        SettableFuture<MultipartPart> response = SettableFuture.create();
        response.set(MultipartPart.create(partNumber, partSize, eTag));
        return response;
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

        return Futures.transform(response, new Function<ListBlobBlocksResponse, List<MultipartPart>>() {
            @Override
            public List<MultipartPart> apply(ListBlobBlocksResponse input) {
                ImmutableList.Builder<MultipartPart> parts = ImmutableList.builder();
                for (BlobBlockProperties properties : input.getBlocks()) {
                    int partNumber = Ints.fromByteArray(BaseEncoding.base64().decode(properties.getBlockName()));
                    String eTag = "";  // getBlockList does not return ETag
                    long partSize = -1;  // TODO: could call getContentLength but did not above
                    parts.add(MultipartPart.create(partNumber, partSize, eTag));
                }

                return parts.build();
            }
        });
    }
}
