package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback coaching windows by code. */
interface FallbackCoachingWindowStore {
    fun find(code: String): FallbackCoachingWindow?
    fun save(item: FallbackCoachingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCoachingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCoachingWindowStore(
    var items: MutableMap<String, FallbackCoachingWindow>,
    var order: MutableList<String>,
) : FallbackCoachingWindowStore {
    override fun find(code: String): FallbackCoachingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCoachingWindow) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<FallbackCoachingWindow> {
        val out = mutableListOf<FallbackCoachingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCoachingWindowStore(): FallbackCoachingWindowStore {
    return MemoryFallbackCoachingWindowStore(
        mutableMapOf<String, FallbackCoachingWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCoachingWindowStore(
    var inner: FallbackCoachingWindowStore,
    var cache: TtlCache<FallbackCoachingWindow>,
    var hits: Int,
    var misses: Int,
) : FallbackCoachingWindowStore {
    override fun find(code: String): FallbackCoachingWindow? {
        val hit = (cache.get(code) as FallbackCoachingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCoachingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCoachingWindow> = inner.list()
}

fun newCachedFallbackCoachingWindowStore(inner: FallbackCoachingWindowStore, ttl: Int, clock: Clock): FallbackCoachingWindowStore {
    return CachedFallbackCoachingWindowStore(
        inner = inner,
        cache = newTtlCache<FallbackCoachingWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback coaching windows. Empty fields match all. */
data class FallbackCoachingWindowQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCoachingWindow): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCoachingWindow>): MutableList<FallbackCoachingWindow> {
        val matched = filter(items) { candidate: FallbackCoachingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCoachingWindows(store: FallbackCoachingWindowStore, query: FallbackCoachingWindowQuery): MutableList<FallbackCoachingWindow> {
    return query.apply(store.list())
}
