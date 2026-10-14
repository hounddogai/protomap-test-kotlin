package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel surcharge summaries by code. */
interface TieredFuelSurchargeSummaryStore {
    fun find(code: String): TieredFuelSurchargeSummary?
    fun save(item: TieredFuelSurchargeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelSurchargeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelSurchargeSummaryStore(
    var items: MutableMap<String, TieredFuelSurchargeSummary>,
    var order: MutableList<String>,
) : TieredFuelSurchargeSummaryStore {
    override fun find(code: String): TieredFuelSurchargeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelSurchargeSummary) {
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

    override fun list(): MutableList<TieredFuelSurchargeSummary> {
        val out = mutableListOf<TieredFuelSurchargeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelSurchargeSummaryStore(): TieredFuelSurchargeSummaryStore {
    return MemoryTieredFuelSurchargeSummaryStore(
        mutableMapOf<String, TieredFuelSurchargeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelSurchargeSummaryStore(
    var inner: TieredFuelSurchargeSummaryStore,
    var cache: TtlCache<TieredFuelSurchargeSummary>,
    var hits: Int,
    var misses: Int,
) : TieredFuelSurchargeSummaryStore {
    override fun find(code: String): TieredFuelSurchargeSummary? {
        val hit = (cache.get(code) as TieredFuelSurchargeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelSurchargeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelSurchargeSummary> = inner.list()
}

fun newCachedTieredFuelSurchargeSummaryStore(inner: TieredFuelSurchargeSummaryStore, ttl: Int, clock: Clock): TieredFuelSurchargeSummaryStore {
    return CachedTieredFuelSurchargeSummaryStore(
        inner = inner,
        cache = newTtlCache<TieredFuelSurchargeSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel surcharge summaries. Empty fields match all. */
data class TieredFuelSurchargeSummaryQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelSurchargeSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFuelSurchargeSummary>): MutableList<TieredFuelSurchargeSummary> {
        val matched = filter(items) { candidate: TieredFuelSurchargeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelSurchargeSummaries(store: TieredFuelSurchargeSummaryStore, query: TieredFuelSurchargeSummaryQuery): MutableList<TieredFuelSurchargeSummary> {
    return query.apply(store.list())
}
