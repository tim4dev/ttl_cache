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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class CacheTest {

    @Test
    fun `Cache test`() {
        runBlocking {
            val cache = TtlCache.Builder().build()
            val timeStart = System.currentTimeMillis()

            cache.put(
                key = MyDataOne.key,
                value = MyDataOne(id = 1, name = "name 1"),
                timeToLive = 50.milliseconds

            )
            cache.put(
                key = MyDataTwo.key,
                value = MyDataTwo(count = 111, login = "login 1"),
                timeToLive = 100.milliseconds
            )

            println("1 = ${System.currentTimeMillis() - timeStart}")
            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo 1
            cache.get<MyDataTwo>(MyDataTwo.key)?.count shouldBeEqualTo 111
            cache.size shouldBeEqualTo 2

            delay(15)

            println("2 = ${System.currentTimeMillis() - timeStart}")
            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo 1
            cache.get<MyDataTwo>(MyDataTwo.key)?.count shouldBeEqualTo 111
            cache.size shouldBeEqualTo 2

            delay(20)

            println("3 = ${System.currentTimeMillis() - timeStart}")
            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo null
            cache.get<MyDataTwo>(MyDataTwo.key)?.count shouldBeEqualTo 111
            cache.size shouldBeEqualTo 1

            delay(10)

            println("4 = ${System.currentTimeMillis() - timeStart}")
            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo null
            cache.get<MyDataTwo>(MyDataTwo.key)?.count shouldBeEqualTo 111
            cache.size shouldBeEqualTo 1

            delay(25)

            println("5 = ${System.currentTimeMillis() - timeStart}")
            cache.get<MyDataOne>(MyDataOne.key)?.id shouldBeEqualTo null
            cache.get<MyDataTwo>(MyDataTwo.key)?.count shouldBeEqualTo null
            cache.size shouldBeEqualTo 0
        }
    }

    @Test
    fun `Cache expired`() {
        runBlocking {
            val cache = TtlCache.Builder().build()

            cache.put(
                key = MyDataOne.key,
                value = MyDataOne(id = 1, name = "name 1"),
                timeToLive = 1.milliseconds

            )
            cache.put(
                key = MyDataTwo.key,
                value = MyDataTwo(count = 111, login = "login 1"),
                timeToLive = 1.milliseconds
            )

            delay(5)

            cache.size shouldBeEqualTo 2
            cache.get<MyDataOne>(MyDataOne.key) shouldBeEqualTo null
            cache.get<MyDataTwo>(MyDataTwo.key) shouldBeEqualTo null
            cache.size shouldBeEqualTo 0
        }
    }

    @Test
    fun `Bulk cached and expired`() {
        runBlocking {
            val cache = TtlCache.Builder().build()
            val count = 100

            repeat(count) { idx ->
                cache.put(
                    key = MyDataOne.key + "$idx",
                    value = MyDataOne(id = idx, name = "name $idx"),
                    timeToLive = 50.milliseconds
                )
            }

            delay(10)

            cache.removeExpired()
            cache.size shouldBeEqualTo count

            repeat(count) { idx ->
                cache.get<MyDataOne>(MyDataOne.key + "$idx")?.id shouldBeEqualTo idx
            }

            delay(55)
            cache.removeExpired()
            cache.size shouldBeEqualTo 0
        }
    }

    @Test
    fun `Concurrency cached and expired`() {
        runBlocking {
            val cache = TtlCache.Builder().build()

            withContext(Dispatchers.IO) {
                massiveRun(10, 10) { idxCoroutine, idxRepeat ->
                    val idx = (idxCoroutine + 1) * 1000 + (idxRepeat + 1)
                    cache.put(
                        key = MyDataOne.key + "$idx",
                        value = MyDataOne(id = idx, name = "name"),
                        timeToLive = 50.milliseconds
                    )
                }
            }

            delay(10)

            cache.size shouldBeEqualTo 100
            cache.removeExpired()
            cache.size shouldBeEqualTo 100

            delay(55)
            cache.removeExpired()
            cache.size shouldBeEqualTo 0
        }
    }

}
