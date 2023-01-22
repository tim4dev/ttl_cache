/*
 * Copyright (c) 2023 Yuriy Timofeev <developer.tim4dev@gmail.com>
 *
 * Distributed under the Apache License 2.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package dev.tim4.ttl_cache

import dev.tim4.ttl_cache.internal.TtlCacheBuilderImpl.Companion.MAX_SIZE_INFINITE
import dev.tim4.ttl_cache.internal.TtlCacheImpl
import dev.tim4.ttl_cache.lib.TtlCache
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.Test

class CacheBuilderTest {

    @Test
    fun `Builder default`() {
        val cache = TtlCache.Builder().build() as TtlCacheImpl
        cache.maxSize shouldBeEqualTo MAX_SIZE_INFINITE
    }

    @Test
    fun `Builder maxSize`() {
        val cache = TtlCache.Builder()
            .maxCacheSize(100)
            .build() as TtlCacheImpl
        cache.maxSize shouldBeEqualTo 100
    }

    @Test
    fun `Builder maxSize exception`() {
        invoking {
            TtlCache.Builder()
                .maxCacheSize(-1)
                .build()
        } shouldThrow IllegalArgumentException::class
    }
}
