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

package org.jclouds.azureblob.blobstore.functions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import org.jclouds.azureblob.domain.BlobBlockProperties;
import org.jclouds.azureblob.domain.ListBlobBlocksResponse;
import org.jclouds.blobstore.domain.MultipartPart;

import java.util.List;

public class AzureListBlobBlocksResponseToMultipartList
        implements Function<ListBlobBlocksResponse, List<MultipartPart>> {
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
}
