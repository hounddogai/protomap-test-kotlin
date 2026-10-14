package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted node windows by code. */
interface WeightedNodeWindowStore {
    fun find(code: String): WeightedNodeWindow?
    fun save(item: WeightedNodeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedNodeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedNodeWindowStore(
    var items: MutableMap<String, WeightedNodeWindow>,
    var order: MutableList<String>,
) : WeightedNodeWindowStore {
    override fun find(code: String): WeightedNodeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedNodeWindow) {
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

    override fun list(): MutableList<WeightedNodeWindow> {
        val out = mutableListOf<WeightedNodeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedNodeWindowStore(): WeightedNodeWindowStore {
    return MemoryWeightedNodeWindowStore(mutableMapOf<String, WeightedNodeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedNodeWindowStore(
    var inner: WeightedNodeWindowStore,
    var cache: TtlCache<WeightedNodeWindow>,
    var hits: Int,
    var misses: Int,
) : WeightedNodeWindowStore {
    override fun find(code: String): WeightedNodeWindow? {
        val hit = (cache.get(code) as WeightedNodeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedNodeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedNodeWindow> = inner.list()
}

fun newCachedWeightedNodeWindowStore(inner: WeightedNodeWindowStore, ttl: Int, clock: Clock): WeightedNodeWindowStore {
    return CachedWeightedNodeWindowStore(
        inner = inner,
        cache = newTtlCache<WeightedNodeWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted node windows. Empty fields match all. */
data class WeightedNodeWindowQuery(
    var segment: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedNodeWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedNodeWindow>): MutableList<WeightedNodeWindow> {
        val matched = filter(items) { candidate: WeightedNodeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedNodeWindows(store: WeightedNodeWindowStore, query: WeightedNodeWindowQuery): MutableList<WeightedNodeWindow> {
    return query.apply(store.list())
}
