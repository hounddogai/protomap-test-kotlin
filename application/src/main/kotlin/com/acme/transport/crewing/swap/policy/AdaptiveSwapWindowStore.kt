package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap windows by code. */
interface AdaptiveSwapWindowStore {
    fun find(code: String): AdaptiveSwapWindow?
    fun save(item: AdaptiveSwapWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapWindowStore(
    var items: MutableMap<String, AdaptiveSwapWindow>,
    var order: MutableList<String>,
) : AdaptiveSwapWindowStore {
    override fun find(code: String): AdaptiveSwapWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapWindow) {
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

    override fun list(): MutableList<AdaptiveSwapWindow> {
        val out = mutableListOf<AdaptiveSwapWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapWindowStore(): AdaptiveSwapWindowStore {
    return MemoryAdaptiveSwapWindowStore(mutableMapOf<String, AdaptiveSwapWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapWindowStore(
    var inner: AdaptiveSwapWindowStore,
    var cache: TtlCache<AdaptiveSwapWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapWindowStore {
    override fun find(code: String): AdaptiveSwapWindow? {
        val hit = (cache.get(code) as AdaptiveSwapWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapWindow> = inner.list()
}

fun newCachedAdaptiveSwapWindowStore(inner: AdaptiveSwapWindowStore, ttl: Int, clock: Clock): AdaptiveSwapWindowStore {
    return CachedAdaptiveSwapWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap windows. Empty fields match all. */
data class AdaptiveSwapWindowQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapWindow>): MutableList<AdaptiveSwapWindow> {
        val matched = filter(items) { candidate: AdaptiveSwapWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapWindows(store: AdaptiveSwapWindowStore, query: AdaptiveSwapWindowQuery): MutableList<AdaptiveSwapWindow> {
    return query.apply(store.list())
}
