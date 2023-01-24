/*
 * Copyright (c) 2023 Yuriy Timofeev <developer.tim4dev@gmail.com>
 *
 * Distributed under the Apache License 2.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package dev.tim4.ttl_cache.lib

import dev.tim4.ttl_cache.internal.TtlCacheBuilderImpl
import kotlin.time.Duration

/**
 * Very simple in-memory key-value cache with time-to-live (TTL) support.
 *
 * Each object in the cache has its own time-to-live (TTL).
 *
 * Objects with an expired TTL are stored in the cache,
 * but will then be deleted the first time they are accessed.
 *
 * The cache is automatically cleared of obsolete objects manually or
 * when the maximum size is reached when new objects arrive.
 *
 * A simple size limit -- objects over the specified number are not cached.
 * In this case, the cache will try to delete objects with an expired TTL and
 * place a new object in the cache.
 * The default is no size limit.
 */
interface TtlCache {

    /**
     * Current cache size
     */
    val size: Int

    /**
     * If the specified [key] already exists in the cache,
     * it will be overwritten with the new [value] and [timeToLive]
     */
    fun put(
        key: String,
        value: Any,
        timeToLive: Duration
    )

    fun <T> get(key: String): T?

    /**
     * If the object is not obtained from the cache,
     * then [loader] will be run to obtain the object from the external source.
     *
     * The resulting object will be cached.
     * All exceptions thrown in [loader] will be passed to the caller.
     *
     * @param timeToLive TTL for caching the object loaded by the [loader]
     */
    suspend fun <T> getOrLoad(
        key: String,
        timeToLive: Duration,
        loader: suspend () -> T?
    ): T?

    fun remove(key: String)

    fun removeExpired()

    /**
     * Remove all of the elements from the cache
     */
    fun clearCache()

    interface Builder {

        fun maxCacheSize(size: Long): Builder

        fun build(): TtlCache

        companion object {
            operator fun invoke(): Builder = TtlCacheBuilderImpl()
        }
    }
}
