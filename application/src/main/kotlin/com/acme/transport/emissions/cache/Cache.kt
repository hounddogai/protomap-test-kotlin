package com.acme.transport.emissions.cache

import com.acme.transport.emissions.clock.Clock

// An in-memory cache with a time to live and a capacity.

/** TtlCache keeps up to capacity values for ttl seconds each, evicting the oldest first. */
class TtlCache<V>(
    val get: (String) -> V?,
    val put: (String, V) -> Unit,
    val size: () -> Int,
    val purgeExpired: () -> Int,
)

fun <V> newTtlCache(capacity: Int, ttl: Int, clock: Clock): TtlCache<V> {
    val entries = mutableMapOf<String, V>()
    val expires = mutableMapOf<String, Int>()
    var order = mutableListOf<String>()
    return TtlCache(
        get = fun(key: String): V? {
            val deadline = expires[key] ?: return null
            if (clock.now() > deadline) {
                entries.remove(key)
                expires.remove(key)
                return null
            }
            val value = entries[key] ?: return null
            return value
        },
        put = fun(key: String, value: V) {
            if (!entries.containsKey(key)) {
                order.add(key)
            }
            entries[key] = value
            expires[key] = clock.now() + ttl
            while (order.size > capacity && order.size > 0) {
                val oldest: String = order[0]
                order = order.drop(1).toMutableList()
                entries.remove(oldest)
                expires.remove(oldest)
            }
        },
        size = fun(): Int {
            return entries.size
        },
        purgeExpired = fun(): Int {
            val now = clock.now()
            val kept = mutableListOf<String>()
            var removed = 0
            for (key in order) {
                val deadline = expires[key] ?: 0
                if (now > deadline) {
                    entries.remove(key)
                    expires.remove(key)
                    removed += 1
                    continue
                }
                kept.add(key)
            }
            order = kept
            return removed
        },
    )
}
