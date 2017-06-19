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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.internal.AsyncBaseBlobStore;
import org.jclouds.blobstore.util.BlobUtils;

public class AsyncAzureBlobStore extends AsyncBaseBlobStore {
    private final AzureBlobClient client;
    private final BlobToAzureBlob blob2AzureBlob;

    @Inject
    AsyncAzureBlobStore(BlobStoreContext context, BlobUtils blobUtils, AzureBlobClient client,
       BlobToAzureBlob blob2AzureBlob) {
        super(context, blobUtils);
        this.client = client;
        this.blob2AzureBlob = blob2AzureBlob;
    }

    @Override
    public ListenableFuture<String> putBlob(String container, Blob blob) {
        return client.putBlobAsync(container, blob2AzureBlob.apply(blob));
    }
}
