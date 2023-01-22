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
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

internal class TtlCacheImpl(
    val maxSize: Long
) : TtlCache {

    private val cacheEntries = ConcurrentHashMap<String, CacheData<*>>()

    override val size: Int
        get() = cacheEntries.size

    private val isMaxSize = maxSize > 0

    private val isOversize: Boolean
        get() = isMaxSize && (size >= maxSize)

    override fun put(key: String, value: Any, timeToLive: Duration) {
        if (timeToLive.isPositive()) {
            if (isOversize) removeExpired()
            if (isOversize.not()) cacheEntries[key] =
                CacheData(data = value, timeToLive = timeToLive)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        return when (val data = this.cacheEntries[key]?.get() as? T) {
            null -> {
                remove(key)
                null
            }
            else -> data
        }
    }

    override fun remove(key: String) {
        cacheEntries.remove(key)
    }

    override fun removeExpired() {
        cacheEntries.entries.removeIf { it.value.isExpired() }
    }

    override fun clearCache() {
        cacheEntries.clear()
    }

    override suspend fun <T> getOrLoad(
        key: String,
        timeToLive: Duration,
        loader: suspend () -> T?
    ): T? {
        return get<T>(key) ?: loader()?.let { data ->
            put(
                key = key,
                value = data,
                timeToLive = timeToLive
            )
            data
        }
    }

}
