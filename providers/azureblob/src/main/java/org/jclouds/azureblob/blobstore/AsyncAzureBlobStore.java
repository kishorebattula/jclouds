package org.jclouds.azureblob.blobstore;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.jclouds.azureblob.AzureBlobClient;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.internal.AsyncBaseBlobStore;
import org.jclouds.blobstore.util.BlobUtils;

/**
 * Created by battula on 25/04/17.
 */
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
