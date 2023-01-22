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

import dev.tim4.ttl_cache.lib.TtlCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UsageTest {

    @Test
    fun `usage example`() {
        runBlocking {
            val cache = TtlCache.Builder()
                .maxCacheSize(100) // The default is no cache size limit
                .build()
            val repo = Repository(cache)

            val data = repo.loadSomeData()!!

            cache.size shouldBeEqualTo 1
            data.size shouldBeEqualTo 3
            data[0].id shouldBeEqualTo 1
        }
    }

    @Test
    fun `simple usage example`() {
        val cache = TtlCache.Builder().build()

        cache.put(
            key = "SomeData_key",
            value = SomeData(id = 1, name = "1"),
            timeToLive = 5.minutes
        )

        val data = cache.get<SomeData>("SomeData_key")
    }

    // ---------------------------------------------------------------------------------------------

    private data class SomeData(
        val id: Int,
        val name: String
    )

    private class Repository(
        private val cache: TtlCache
    ) {
        private enum class CacheType(
            val mapKey: String,
            val ttl: Duration
        ) {
            SOME_DATA("loadSomeData", 1.minutes + 30.seconds);
        }

        suspend fun loadSomeData(): List<SomeData>? {
            /**
             * If the object is not obtained from the cache,
             * then "loader" will be run to obtain the object from the external source.
             * The resulting object will be cached.
             */
            return cache.getOrLoad(
                key = CacheType.SOME_DATA.mapKey,
                timeToLive = CacheType.SOME_DATA.ttl // TTL for caching the object loaded
            ) {
                // loader
                loadRemoteData()
            }
        }

        private suspend fun loadRemoteData(): List<SomeData>? {
            delay(100)
            return listOf(
                SomeData(id = 1, name = "t"),
                SomeData(id = 2, name = "te"),
                SomeData(id = 3, name = "test")
            )
        }
    }

}
