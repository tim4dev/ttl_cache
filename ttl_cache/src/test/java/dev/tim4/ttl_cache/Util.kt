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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

internal data class MyDataOne(
    val id: Int,
    val name: String
) {
    companion object {
        const val key = "dev.tim4.ttl_cache.MyDataOne"
    }
}

internal data class MyDataTwo(
    val count: Int,
    val login: String
) {
    companion object {
        const val key = "dev.tim4.ttl_cache.MyDataTwo"
    }
}

internal data class MyDataThree(
    val term: Int,
    val sum: Double
) {
    companion object {
        const val key = "dev.tim4.ttl_cache.MyDataThree"
    }
}

// -------------------------------------------------------------------------------------------------

internal suspend fun massiveRun(
    /**
     * number of coroutines to launch
     */
    maxCoroutines: Int = 10,

    /**
     * times an action is repeated by each coroutine
     */
    maxRepeat: Int = 10,

    action: suspend (Int, Int) -> Unit
) {
    val time = measureTimeMillis {
        coroutineScope {
            repeat(maxCoroutines) { idxCoroutine ->
                launch {
                    repeat(maxRepeat) { idxRepeat ->
                        action(idxCoroutine, idxRepeat)
                    }
                }
            }
        }
    }
    println("Completed ${maxCoroutines * maxRepeat} actions in $time ms")
}

// -------------------------------------------------------------------------------------------------

/**
 * Usage:
 * <pre>
 * val data = getData(uri).orElse { DEFAULT_VALUE }
 * </pre>
 */
inline fun <T> T?.orElse(block: () -> T) = this ?: block.invoke()

fun divider() {
    println("-".repeat(50))
}
