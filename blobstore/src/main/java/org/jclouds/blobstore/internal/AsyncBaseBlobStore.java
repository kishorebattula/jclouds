package org.jclouds.blobstore.internal;

import com.google.inject.Inject;
import org.jclouds.blobstore.AsyncBlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.util.BlobUtils;
import org.jclouds.blobstore.util.internal.BlobUtilsImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by battula on 25/04/17.
 */
public abstract class AsyncBaseBlobStore implements AsyncBlobStore {
    protected final BlobStoreContext context;
    protected final BlobUtils blobUtils;

    @Inject
    protected AsyncBaseBlobStore(BlobStoreContext context, BlobUtils blobUtils) {
        this.context = checkNotNull(context, "context");
        this.blobUtils = checkNotNull(blobUtils, "blobUtils");
    }

    @Override
    public BlobStoreContext getContext() {
        return context;
    }

    /**
     * invokes {@link BlobUtilsImpl#blobBuilder }
     */
    @Override
    public BlobBuilder blobBuilder(String name) {
        return blobUtils.blobBuilder().name(name);
    }
}
