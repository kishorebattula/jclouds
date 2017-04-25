package org.jclouds.blobstore;

/**
 * Created by battula on 25/04/17.
 */

import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;

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
}
