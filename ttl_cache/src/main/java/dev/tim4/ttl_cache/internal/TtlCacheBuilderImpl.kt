/*
 * Copyright (c) 2023 Yuriy Timofeev <developer.tim4dev@gmail.com>
 *
 * Distributed under the Apache License 2.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package dev.tim4.ttl_cache.internal

import dev.tim4.ttl_cache.lib.TtlCache

internal class TtlCacheBuilderImpl : TtlCache.Builder {

    private var maxSize = MAX_SIZE_INFINITE

    override fun maxCacheSize(size: Long): TtlCache.Builder = apply {
        require(size > 0) {
            "maxCacheSize must be greater than 0"
        }
        this.maxSize = size
    }

    override fun build(): TtlCache {
        return TtlCacheImpl(maxSize)
    }

    companion object {
        internal const val MAX_SIZE_INFINITE: Long = -1
    }
}
