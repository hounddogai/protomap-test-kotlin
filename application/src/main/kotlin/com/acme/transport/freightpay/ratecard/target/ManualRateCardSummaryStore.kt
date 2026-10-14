package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual rate card summaries by code. */
interface ManualRateCardSummaryStore {
    fun find(code: String): ManualRateCardSummary?
    fun save(item: ManualRateCardSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRateCardSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRateCardSummaryStore(
    var items: MutableMap<String, ManualRateCardSummary>,
    var order: MutableList<String>,
) : ManualRateCardSummaryStore {
    override fun find(code: String): ManualRateCardSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRateCardSummary) {
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

    override fun list(): MutableList<ManualRateCardSummary> {
        val out = mutableListOf<ManualRateCardSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRateCardSummaryStore(): ManualRateCardSummaryStore {
    return MemoryManualRateCardSummaryStore(
        mutableMapOf<String, ManualRateCardSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRateCardSummaryStore(
    var inner: ManualRateCardSummaryStore,
    var cache: TtlCache<ManualRateCardSummary>,
    var hits: Int,
    var misses: Int,
) : ManualRateCardSummaryStore {
    override fun find(code: String): ManualRateCardSummary? {
        val hit = (cache.get(code) as ManualRateCardSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRateCardSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRateCardSummary> = inner.list()
}

fun newCachedManualRateCardSummaryStore(inner: ManualRateCardSummaryStore, ttl: Int, clock: Clock): ManualRateCardSummaryStore {
    return CachedManualRateCardSummaryStore(
        inner = inner,
        cache = newTtlCache<ManualRateCardSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual rate card summaries. Empty fields match all. */
data class ManualRateCardSummaryQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRateCardSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRateCardSummary>): MutableList<ManualRateCardSummary> {
        val matched = filter(items) { candidate: ManualRateCardSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRateCardSummaries(store: ManualRateCardSummaryStore, query: ManualRateCardSummaryQuery): MutableList<ManualRateCardSummary> {
    return query.apply(store.list())
}
