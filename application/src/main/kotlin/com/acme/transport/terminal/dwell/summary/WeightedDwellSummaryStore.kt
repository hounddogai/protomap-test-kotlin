package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dwell summaries by code. */
interface WeightedDwellSummaryStore {
    fun find(code: String): WeightedDwellSummary?
    fun save(item: WeightedDwellSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDwellSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDwellSummaryStore(
    var items: MutableMap<String, WeightedDwellSummary>,
    var order: MutableList<String>,
) : WeightedDwellSummaryStore {
    override fun find(code: String): WeightedDwellSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDwellSummary) {
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

    override fun list(): MutableList<WeightedDwellSummary> {
        val out = mutableListOf<WeightedDwellSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDwellSummaryStore(): WeightedDwellSummaryStore {
    return MemoryWeightedDwellSummaryStore(
        mutableMapOf<String, WeightedDwellSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDwellSummaryStore(
    var inner: WeightedDwellSummaryStore,
    var cache: TtlCache<WeightedDwellSummary>,
    var hits: Int,
    var misses: Int,
) : WeightedDwellSummaryStore {
    override fun find(code: String): WeightedDwellSummary? {
        val hit = (cache.get(code) as WeightedDwellSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDwellSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDwellSummary> = inner.list()
}

fun newCachedWeightedDwellSummaryStore(inner: WeightedDwellSummaryStore, ttl: Int, clock: Clock): WeightedDwellSummaryStore {
    return CachedWeightedDwellSummaryStore(
        inner = inner,
        cache = newTtlCache<WeightedDwellSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dwell summaries. Empty fields match all. */
data class WeightedDwellSummaryQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDwellSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDwellSummary>): MutableList<WeightedDwellSummary> {
        val matched = filter(items) { candidate: WeightedDwellSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDwellSummaries(store: WeightedDwellSummaryStore, query: WeightedDwellSummaryQuery): MutableList<WeightedDwellSummary> {
    return query.apply(store.list())
}
