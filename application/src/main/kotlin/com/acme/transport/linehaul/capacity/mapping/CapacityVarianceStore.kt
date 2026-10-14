package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity variances by code. */
interface CapacityVarianceStore {
    fun find(code: String): CapacityVariance?
    fun save(item: CapacityVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityVarianceStore(
    var items: MutableMap<String, CapacityVariance>,
    var order: MutableList<String>,
) : CapacityVarianceStore {
    override fun find(code: String): CapacityVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityVariance) {
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

    override fun list(): MutableList<CapacityVariance> {
        val out = mutableListOf<CapacityVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityVarianceStore(): CapacityVarianceStore {
    return MemoryCapacityVarianceStore(mutableMapOf<String, CapacityVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityVarianceStore(
    var inner: CapacityVarianceStore,
    var cache: TtlCache<CapacityVariance>,
    var hits: Int,
    var misses: Int,
) : CapacityVarianceStore {
    override fun find(code: String): CapacityVariance? {
        val hit = (cache.get(code) as CapacityVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityVariance> = inner.list()
}

fun newCachedCapacityVarianceStore(inner: CapacityVarianceStore, ttl: Int, clock: Clock): CapacityVarianceStore {
    return CachedCapacityVarianceStore(
        inner = inner,
        cache = newTtlCache<CapacityVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity variances. Empty fields match all. */
data class CapacityVarianceQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CapacityVariance>): MutableList<CapacityVariance> {
        val matched = filter(items) { candidate: CapacityVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityVariances(store: CapacityVarianceStore, query: CapacityVarianceQuery): MutableList<CapacityVariance> {
    return query.apply(store.list())
}
