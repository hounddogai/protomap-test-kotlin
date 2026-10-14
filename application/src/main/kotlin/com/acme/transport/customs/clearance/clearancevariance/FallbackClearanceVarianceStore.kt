package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance variances by code. */
interface FallbackClearanceVarianceStore {
    fun find(code: String): FallbackClearanceVariance?
    fun save(item: FallbackClearanceVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearanceVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearanceVarianceStore(
    var items: MutableMap<String, FallbackClearanceVariance>,
    var order: MutableList<String>,
) : FallbackClearanceVarianceStore {
    override fun find(code: String): FallbackClearanceVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearanceVariance) {
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

    override fun list(): MutableList<FallbackClearanceVariance> {
        val out = mutableListOf<FallbackClearanceVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearanceVarianceStore(): FallbackClearanceVarianceStore {
    return MemoryFallbackClearanceVarianceStore(
        mutableMapOf<String, FallbackClearanceVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearanceVarianceStore(
    var inner: FallbackClearanceVarianceStore,
    var cache: TtlCache<FallbackClearanceVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackClearanceVarianceStore {
    override fun find(code: String): FallbackClearanceVariance? {
        val hit = (cache.get(code) as FallbackClearanceVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearanceVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearanceVariance> = inner.list()
}

fun newCachedFallbackClearanceVarianceStore(inner: FallbackClearanceVarianceStore, ttl: Int, clock: Clock): FallbackClearanceVarianceStore {
    return CachedFallbackClearanceVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackClearanceVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance variances. Empty fields match all. */
data class FallbackClearanceVarianceQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearanceVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackClearanceVariance>): MutableList<FallbackClearanceVariance> {
        val matched = filter(items) { candidate: FallbackClearanceVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearanceVariances(store: FallbackClearanceVarianceStore, query: FallbackClearanceVarianceQuery): MutableList<FallbackClearanceVariance> {
    return query.apply(store.list())
}
