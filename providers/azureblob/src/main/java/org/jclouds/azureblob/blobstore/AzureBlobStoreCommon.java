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
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.io.MutableContentMetadata;

import java.util.List;
import java.util.UUID;

class AzureBlobStoreCommon {
    static final long MINIMUM_MULTIPART_SIZE = 1;
    static final long MAXIMUM_MULTIPART_SIZE = 100 * 1024 * 1024;
    static final int MAXIMUM_NUMBER_OF_PARTS = 50 * 1000;
    static MultipartUpload initiateMultipartUpload(String container, BlobMetadata blobMetadata,
          PutOptions options) {
        String uploadId = UUID.randomUUID().toString();
        return MultipartUpload.create(container, blobMetadata.getName(), uploadId, blobMetadata, options);
    }

    static AzureBlob prepareDummyBlobForMultipartComplete(MultipartUpload mpu, AzureBlob azureBlob) {
        // fake values to satisfy BindAzureBlobMetadataToMultipartRequest
        azureBlob.setPayload(new byte[0]);
        azureBlob.getProperties().setContainer(mpu.containerName());
        azureBlob.getProperties().setName(mpu.blobName());

        azureBlob.getProperties().setContentMetadata((MutableContentMetadata) mpu.blobMetadata().getContentMetadata());
        azureBlob.getProperties().setMetadata(mpu.blobMetadata().getUserMetadata());
        return azureBlob;
    }

    static List<String> prepareBlockList(List<MultipartPart> parts) {
        ImmutableList.Builder<String> blocks = ImmutableList.builder();
        for (MultipartPart part : parts) {
            String blockId = BaseEncoding.base64().encode(Ints.toByteArray(part.partNumber()));
            blocks.add(blockId);
        }

        return blocks.build();
    }

    static String prepareBlockId(int partNumber) {
        return BaseEncoding.base64().encode(Ints.toByteArray(partNumber));
    }

    static MultipartPart prepareMultipartResponse(int partNumber) {
        String eTag = "";  // putBlock does not return ETag
        long partSize = -1;  // TODO: how to get this from payload?
        return MultipartPart.create(partNumber, partSize, eTag);
    }
}
