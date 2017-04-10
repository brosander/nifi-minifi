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

package org.apache.nifi.minifi.commons.schema.avro;

import org.apache.nifi.minifi.commons.schema.ContentRepositorySchema;
import org.apache.nifi.minifi.commons.schema.avro.generated.ContentRepository;
import org.apache.nifi.minifi.commons.schema.common.CommonPropertyKeys;

import java.util.Map;

public class ContentRepositorySchemaTranslator extends BaseAvroTranslator<ContentRepository, ContentRepository.Builder, ContentRepositorySchema> {
    public ContentRepositorySchemaTranslator() {
        super(ContentRepositorySchema::new, ContentRepository::newBuilder);
    }

    @Override
    protected void putToMap(Map<String, Object> map, ContentRepository avro) {
        putIfSet(map, ContentRepositorySchema.CONTENT_CLAIM_MAX_APPENDABLE_SIZE_KEY, avro.getContentClaimMaxAppendableSize());
        putIfSet(map, ContentRepositorySchema.CONTENT_CLAIM_MAX_FLOW_FILES_KEY, avro.getContentClaimMaxFlowFiles());
        putIfSet(map, CommonPropertyKeys.ALWAYS_SYNC_KEY, avro.getAlwaysSync());
    }

    @Override
    protected void setOnBuilder(ContentRepository.Builder builder, ContentRepositorySchema schema) {
        builder.setContentClaimMaxAppendableSize(schema.getContentClaimMaxAppendableSize());
        builder.setContentClaimMaxFlowFiles(schema.getContentClaimMaxFlowFiles());
        builder.setAlwaysSync(schema.getAlwaysSync());
    }
}
