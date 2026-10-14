package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict fuel surcharge summaries by code. */
interface StrictFuelSurchargeSummaryStore {
    fun find(code: String): StrictFuelSurchargeSummary?
    fun save(item: StrictFuelSurchargeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFuelSurchargeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFuelSurchargeSummaryStore(
    var items: MutableMap<String, StrictFuelSurchargeSummary>,
    var order: MutableList<String>,
) : StrictFuelSurchargeSummaryStore {
    override fun find(code: String): StrictFuelSurchargeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFuelSurchargeSummary) {
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

    override fun list(): MutableList<StrictFuelSurchargeSummary> {
        val out = mutableListOf<StrictFuelSurchargeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFuelSurchargeSummaryStore(): StrictFuelSurchargeSummaryStore {
    return MemoryStrictFuelSurchargeSummaryStore(
        mutableMapOf<String, StrictFuelSurchargeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFuelSurchargeSummaryStore(
    var inner: StrictFuelSurchargeSummaryStore,
    var cache: TtlCache<StrictFuelSurchargeSummary>,
    var hits: Int,
    var misses: Int,
) : StrictFuelSurchargeSummaryStore {
    override fun find(code: String): StrictFuelSurchargeSummary? {
        val hit = (cache.get(code) as StrictFuelSurchargeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFuelSurchargeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFuelSurchargeSummary> = inner.list()
}

fun newCachedStrictFuelSurchargeSummaryStore(inner: StrictFuelSurchargeSummaryStore, ttl: Int, clock: Clock): StrictFuelSurchargeSummaryStore {
    return CachedStrictFuelSurchargeSummaryStore(
        inner = inner,
        cache = newTtlCache<StrictFuelSurchargeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict fuel surcharge summaries. Empty fields match all. */
data class StrictFuelSurchargeSummaryQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFuelSurchargeSummary): Boolean {
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

    fun apply(items: MutableList<StrictFuelSurchargeSummary>): MutableList<StrictFuelSurchargeSummary> {
        val matched = filter(items) { candidate: StrictFuelSurchargeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFuelSurchargeSummaries(store: StrictFuelSurchargeSummaryStore, query: StrictFuelSurchargeSummaryQuery): MutableList<StrictFuelSurchargeSummary> {
    return query.apply(store.list())
}
