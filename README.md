
# Simple Time-To-Live Cache [Android]

[![](https://jitpack.io/v/tim4dev/ttl_cache.svg)](https://jitpack.io/#tim4dev/ttl_cache)

Very simple in-memory key-value cache with time-to-live (TTL) support.
Based on `ConcurrentHashMap`.

The library uses `kotlin.time.Duration` API it provides a nice DSL to manipulate time durations.

Each object in the cache has its own time to live (TTL).

Objects with an expired TTL are stored in the cache, but will then be deleted the first time they
are accessed.

The cache is manually cleared of obsolete objects or when the maximum size is reached when new
objects arrive.

A simple capacity limit -- objects over the specified capacity are not cached. In this case, the
cache will try to delete objects with an expired TTL and place a new object in the cache.

The default is no cache size limit.


# Gradle

Add library to your project dependencies:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation "com.github.tim4dev:ttl_cache:VERSION"
}
```


# Usage

```kotlin
val cache = TtlCache.Builder()
    .maxCacheSize(100) // The default is no cache size limit
    .build()
```

Data classes, for example

```kotlin
data class SomeData(
    val id: Int,
    val name: String
)

data class OtherData(
    val description: String,
    val sum: Double
)
```

Data layer

```kotlin
import kotlin.time.Duration

class Repository(
    private val cache: TtlCache
) {
    private enum class CacheType(
        val mapKey: String,
        val ttl: Duration
    ) {
        SOME_DATA("loadSomeData", 1.minutes + 30.seconds),
        OTHER_DATA("loadOtherData", 5.minutes);
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
            loadRemoteSomeData()
        }
    }

    suspend fun loadOtherData(): OtherData? {
        return cache.getOrLoad(
            key = CacheType.OTHER_DATA.mapKey,
            timeToLive = CacheType.OTHER_DATA.ttl
        ) {
            loadRemoteOtherData()
        }
    }

    suspend fun loadRemoteSomeData(): List<SomeData>? { ... }

    suspend fun loadRemoteOtherData(): OtherData? { ... }
}
```


# Simple Usage

```kotlin
val cache = TtlCache.Builder().build()

cache.put(
    key = "SomeData_key",
    value = SomeData(...),
    timeToLive = 5.minutes + 30.seconds
)

cache.put(
    key = "OtherData_key",
    value = OtherData(...),
    timeToLive = 10.minutes
)

cache.put(
    key = "AnotherData_key",
    value = AnotherData(...),
    timeToLive = INFINITE // TTL will never expire
)

val dataSome = cache.get<SomeData>("SomeData_key")
val dataOther = cache.get<OtherData>("OtherData_key")
val dataAnother = cache.get<AnotherData>("AnotherData_key")

cache.removeExpired() // remove all expired objects 

cache.clearCache() // remove all objects, after that the cache is empty

```


# License

Copyright (c) 2023 Yuriy Timofeev.
Distributed under the [Apache License 2](http://www.apache.org/licenses/LICENSE-2.0). 
