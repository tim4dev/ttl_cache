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
import org.amshove.kluent.shouldBeTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RepositoryTest {

    @Test
    fun `Cached test`() {
        runBlocking {
            val cache = TtlCache.Builder()
                .maxCacheSize(10)
                .build()

            val repo = RepositoryImpl(cache)

            val timeStart = System.currentTimeMillis()

            repo.loadDataOne(timeToLive = 100.milliseconds).id shouldBeEqualTo 0
            repo.loadDataOne(timeToLive = 100.milliseconds).id shouldBeEqualTo 0
            repo.loadDataOne(timeToLive = 100.milliseconds).id shouldBeEqualTo 0

            repo.cacheSize shouldBeEqualTo 1

            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 0
            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 0
            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 0

            repo.cacheSize shouldBeEqualTo 2

            println("1 = ${System.currentTimeMillis() - timeStart}")
            delay(51)
            repo.loadDataOne(timeToLive = 100.milliseconds).id shouldBeEqualTo 0
            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 1

            repo.cacheSize shouldBeEqualTo 2

            println("2 = ${System.currentTimeMillis() - timeStart}")
            delay(101)
            repo.loadDataOne(timeToLive = 100.milliseconds).id shouldBeEqualTo 1
            repo.loadDataTwo(timeToLive = 100.milliseconds).count shouldBeEqualTo 2

            repo.cacheSize shouldBeEqualTo 2
        }
    }

    @Test
    fun `Invalidate cache test`() {
        runBlocking {
            val cache = TtlCache.Builder()
                .maxCacheSize(10)
                .build()

            val repo = RepositoryImpl(cache)

            repo.loadDataOne(timeToLive = 20.milliseconds).id shouldBeEqualTo 0
            repo.loadDataTwo(timeToLive = 30.milliseconds).count shouldBeEqualTo 0

            repo.cacheSize shouldBeEqualTo 2

            repo.invalidateCache()

            repo.cacheSize shouldBeEqualTo 0

            repo.loadDataOne(timeToLive = 20.milliseconds).id shouldBeEqualTo 1
            repo.loadDataTwo(timeToLive = 30.milliseconds).count shouldBeEqualTo 1

            repo.cacheSize shouldBeEqualTo 2
        }
    }

    @Test
    fun `Oversize test`() {
        runBlocking {
            val cache = TtlCache.Builder()
                .maxCacheSize(2)
                .build()

            val repo = RepositoryImpl(cache)

            repo.loadDataOne(timeToLive = 10.milliseconds).id shouldBeEqualTo 0
            repo.loadDataTwo(timeToLive = 10.milliseconds).count shouldBeEqualTo 0
            repo.loadDataThree(timeToLive = 10.milliseconds).term shouldBeEqualTo 0
            repo.cacheSize shouldBeEqualTo 2

            // -----------------------------------------------------------
            divider()

            delay(20)
            repo.loadDataOne(timeToLive = 10.milliseconds).id shouldBeEqualTo 1
            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 1
            delay(15)
            repo.loadDataThree(timeToLive = 50.milliseconds).term shouldBeEqualTo 1
            repo.cacheSize shouldBeEqualTo 2

            repo.loadDataTwo(timeToLive = 50.milliseconds).count shouldBeEqualTo 1
            repo.loadDataThree(timeToLive = 50.milliseconds).term shouldBeEqualTo 1
        }
    }

    @Test
    fun `No cached test`() {
        runBlocking {
            val cache = TtlCache.Builder().build()
            val repo = RepositoryImpl(cache)

            repo.loadDataOne(timeToLive = 0.milliseconds).id shouldBeEqualTo 0
            delay(2)
            repo.loadDataOne(timeToLive = 0.milliseconds).id shouldBeEqualTo 1
            delay(2)
            repo.loadDataOne(timeToLive = 0.milliseconds).id shouldBeEqualTo 2
            delay(2)
            repo.loadDataOne(timeToLive = 0.milliseconds).id shouldBeEqualTo 3

            delay(2)
            repo.loadDataOne(timeToLive = 1.milliseconds).id shouldBeEqualTo 4
            delay(2)
            repo.loadDataOne(timeToLive = 1.milliseconds).id shouldBeEqualTo 5
            delay(2)
            repo.loadDataOne(timeToLive = 1.milliseconds).id shouldBeEqualTo 6
            delay(2)
            repo.loadDataOne(timeToLive = 1.milliseconds).id shouldBeEqualTo 7

            repo.cacheSize shouldBeEqualTo 1
        }
    }

    @Test
    fun `Bulk test`() {
        runBlocking {
            val cache = TtlCache.Builder().build()
            val repo = RepositoryImpl(cache)

            val count = 100

            repeat(count) {
                repo.loadDataOne(timeToLive = 1.milliseconds)
                delay(3)
            }

            delay(3)

            repo.cacheSize shouldBeEqualTo 1
            repo.loadDataOne(timeToLive = 0.milliseconds).id shouldBeEqualTo count
        }
    }

    @Test
    fun `Concurrency test`() {
        runBlocking {
            val cache = TtlCache.Builder().build()
            val repo = RepositoryImpl(cache)

            withContext(Dispatchers.Default) {
                massiveRun { idxCoroutine, idxRepeat ->
                    repo.loadDataOne(timeToLive = 1.milliseconds).id
                    delay(3)
                }
            }

            delay(3)

            repo.cacheSize shouldBeEqualTo 1
            repo.loadDataOne(timeToLive = 0.milliseconds).id.let {
                println("id = $it")
                (it > 1).shouldBeTrue() // ideally should be == 100
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private interface Repository {
        val cacheSize: Int
        fun loadDataOne(timeToLive: Duration): MyDataOne
        fun loadDataTwo(timeToLive: Duration): MyDataTwo
        fun loadDataThree(timeToLive: Duration): MyDataThree
        fun invalidateCache()
    }

    private class RepositoryImpl(
        private val cache: TtlCache
    ) : Repository {

        override val cacheSize: Int
            get() = cache.size

        override fun loadDataOne(timeToLive: Duration): MyDataOne {
            return cache.get<MyDataOne>(MyDataOne.key).orElse {
                val data = loadRemoteDataOne()
                cache.put(
                    key = MyDataOne.key,
                    value = data,
                    timeToLive = timeToLive
                )
                data
            }
        }

        override fun loadDataTwo(timeToLive: Duration): MyDataTwo {
            return cache.get<MyDataTwo>(MyDataTwo.key).orElse {
                val data = loadRemoteDataTwo()
                cache.put(
                    key = MyDataTwo.key,
                    value = data,
                    timeToLive = timeToLive
                )
                data
            }
        }

        override fun loadDataThree(timeToLive: Duration): MyDataThree {
            return cache.get<MyDataThree>(MyDataThree.key).orElse {
                val data = loadRemoteDataThree()
                cache.put(
                    key = MyDataThree.key,
                    value = data,
                    timeToLive = timeToLive
                )
                data
            }
        }

        override fun invalidateCache() {
            cache.clearCache()
        }

        // ------------------------------------------------

        private var counterOne = AtomicInteger(0)
        private var counterTwo = AtomicInteger(0)
        private var counterThree = AtomicInteger(0)

        private fun loadRemoteDataOne(): MyDataOne {
            return MyDataOne(
                id = counterOne.getAndIncrement(), name = "name"
            )
        }

        private fun loadRemoteDataTwo() = MyDataTwo(
            count = counterTwo.getAndIncrement(), login = "login"
        )

        private fun loadRemoteDataThree() = MyDataThree(
            term = counterThree.getAndIncrement(), sum = 1.0
        )
    }
}
