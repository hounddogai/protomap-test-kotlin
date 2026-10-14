package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary clearance variances by code. */
interface PrimaryClearanceVarianceStore {
    fun find(code: String): PrimaryClearanceVariance?
    fun save(item: PrimaryClearanceVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryClearanceVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryClearanceVarianceStore(
    var items: MutableMap<String, PrimaryClearanceVariance>,
    var order: MutableList<String>,
) : PrimaryClearanceVarianceStore {
    override fun find(code: String): PrimaryClearanceVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryClearanceVariance) {
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

    override fun list(): MutableList<PrimaryClearanceVariance> {
        val out = mutableListOf<PrimaryClearanceVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryClearanceVarianceStore(): PrimaryClearanceVarianceStore {
    return MemoryPrimaryClearanceVarianceStore(
        mutableMapOf<String, PrimaryClearanceVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryClearanceVarianceStore(
    var inner: PrimaryClearanceVarianceStore,
    var cache: TtlCache<PrimaryClearanceVariance>,
    var hits: Int,
    var misses: Int,
) : PrimaryClearanceVarianceStore {
    override fun find(code: String): PrimaryClearanceVariance? {
        val hit = (cache.get(code) as PrimaryClearanceVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryClearanceVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryClearanceVariance> = inner.list()
}

fun newCachedPrimaryClearanceVarianceStore(inner: PrimaryClearanceVarianceStore, ttl: Int, clock: Clock): PrimaryClearanceVarianceStore {
    return CachedPrimaryClearanceVarianceStore(
        inner = inner,
        cache = newTtlCache<PrimaryClearanceVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary clearance variances. Empty fields match all. */
data class PrimaryClearanceVarianceQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryClearanceVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryClearanceVariance>): MutableList<PrimaryClearanceVariance> {
        val matched = filter(items) { candidate: PrimaryClearanceVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryClearanceVariances(store: PrimaryClearanceVarianceStore, query: PrimaryClearanceVarianceQuery): MutableList<PrimaryClearanceVariance> {
    return query.apply(store.list())
}
