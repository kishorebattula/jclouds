package org.jclouds.azureblob.blobstore;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.functions.AzureBlobToBlob;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.functions.BlobToHttpGetOptions;
import org.jclouds.blobstore.internal.AsyncBaseBlobStore;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.http.options.GetOptions;

/**
 * Created by battula on 25/04/17.
 */
public class AsyncAzureBlobStore extends AsyncBaseBlobStore {
    private final AzureBlobClient client;
    private final BlobToAzureBlob blob2AzureBlob;
    private final AzureBlobToBlob azureBlob2Blob;
    private final BlobToHttpGetOptions blob2ObjectGetOptions;

    @Inject
    AsyncAzureBlobStore(BlobStoreContext context, BlobUtils blobUtils, AzureBlobClient client,
       BlobToAzureBlob blob2AzureBlob, AzureBlobToBlob azureBlob2Blob, BlobToHttpGetOptions blob2ObjectGetOptions) {
        super(context, blobUtils);
        this.client = client;
        this.blob2AzureBlob = blob2AzureBlob;
        this.azureBlob2Blob = azureBlob2Blob;
        this.blob2ObjectGetOptions = blob2ObjectGetOptions;
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
}
