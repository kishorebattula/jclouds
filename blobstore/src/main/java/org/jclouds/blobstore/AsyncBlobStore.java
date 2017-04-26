package org.jclouds.blobstore;

/**
 * Created by battula on 25/04/17.
 */

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
