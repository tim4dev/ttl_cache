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
import kotlin.time.Duration.Companion.milliseconds

class GetOrLoadTest {

    @Test
    fun `getOrLoad test`() {
        runBlocking {
            val cache = TtlCache.Builder().build()

            cache.getOrLoad(
                key = MyDataOne.key,
                timeToLive = 20.milliseconds
            ) {
                loadRemoteDataOne(11)
            }

            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo 11

            delay(25)

            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo null
        }
    }

    // ---------------------------------------------------------------------------------------------

    private suspend fun loadRemoteDataOne(id: Int): MyDataOne {
        delay(50)
        return MyDataOne(id = id, name = "name")
    }
}
