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

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
internal class CacheData<T>(
    data: T? = null,
    timeToLive: Duration
) {
    private var _data: T? = data
    private var _expiredTimeMark: TimeMark = TimeSource.Monotonic.markNow() + timeToLive

    fun get(): T? {
        return if (isExpired()) null else _data
    }

    fun isExpired(): Boolean {
        return _expiredTimeMark.hasPassedNow()
    }
}
