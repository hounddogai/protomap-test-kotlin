package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge summaries by code. */
interface FuelSurchargeSummaryStore {
    fun find(code: String): FuelSurchargeSummary?
    fun save(item: FuelSurchargeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeSummaryStore(
    var items: MutableMap<String, FuelSurchargeSummary>,
    var order: MutableList<String>,
) : FuelSurchargeSummaryStore {
    override fun find(code: String): FuelSurchargeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeSummary) {
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

    override fun list(): MutableList<FuelSurchargeSummary> {
        val out = mutableListOf<FuelSurchargeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeSummaryStore(): FuelSurchargeSummaryStore {
    return MemoryFuelSurchargeSummaryStore(
        mutableMapOf<String, FuelSurchargeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeSummaryStore(
    var inner: FuelSurchargeSummaryStore,
    var cache: TtlCache<FuelSurchargeSummary>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeSummaryStore {
    override fun find(code: String): FuelSurchargeSummary? {
        val hit = (cache.get(code) as FuelSurchargeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeSummary> = inner.list()
}

fun newCachedFuelSurchargeSummaryStore(inner: FuelSurchargeSummaryStore, ttl: Int, clock: Clock): FuelSurchargeSummaryStore {
    return CachedFuelSurchargeSummaryStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge summaries. Empty fields match all. */
data class FuelSurchargeSummaryQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeSummary>): MutableList<FuelSurchargeSummary> {
        val matched = filter(items) { candidate: FuelSurchargeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeSummaries(store: FuelSurchargeSummaryStore, query: FuelSurchargeSummaryQuery): MutableList<FuelSurchargeSummary> {
    return query.apply(store.list())
}
