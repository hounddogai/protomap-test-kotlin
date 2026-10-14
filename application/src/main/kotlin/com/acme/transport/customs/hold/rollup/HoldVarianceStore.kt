package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold variances by code. */
interface HoldVarianceStore {
    fun find(code: String): HoldVariance?
    fun save(item: HoldVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldVarianceStore(
    var items: MutableMap<String, HoldVariance>,
    var order: MutableList<String>,
) : HoldVarianceStore {
    override fun find(code: String): HoldVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldVariance) {
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

    override fun list(): MutableList<HoldVariance> {
        val out = mutableListOf<HoldVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldVarianceStore(): HoldVarianceStore {
    return MemoryHoldVarianceStore(mutableMapOf<String, HoldVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldVarianceStore(
    var inner: HoldVarianceStore,
    var cache: TtlCache<HoldVariance>,
    var hits: Int,
    var misses: Int,
) : HoldVarianceStore {
    override fun find(code: String): HoldVariance? {
        val hit = (cache.get(code) as HoldVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldVariance> = inner.list()
}

fun newCachedHoldVarianceStore(inner: HoldVarianceStore, ttl: Int, clock: Clock): HoldVarianceStore {
    return CachedHoldVarianceStore(
        inner = inner,
        cache = newTtlCache<HoldVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold variances. Empty fields match all. */
data class HoldVarianceQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldVariance>): MutableList<HoldVariance> {
        val matched = filter(items) { candidate: HoldVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldVariances(store: HoldVarianceStore, query: HoldVarianceQuery): MutableList<HoldVariance> {
    return query.apply(store.list())
}
