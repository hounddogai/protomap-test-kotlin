package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn summaries by code. */
interface FuelBurnSummaryStore {
    fun find(code: String): FuelBurnSummary?
    fun save(item: FuelBurnSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnSummaryStore(
    var items: MutableMap<String, FuelBurnSummary>,
    var order: MutableList<String>,
) : FuelBurnSummaryStore {
    override fun find(code: String): FuelBurnSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnSummary) {
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

    override fun list(): MutableList<FuelBurnSummary> {
        val out = mutableListOf<FuelBurnSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnSummaryStore(): FuelBurnSummaryStore {
    return MemoryFuelBurnSummaryStore(mutableMapOf<String, FuelBurnSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnSummaryStore(
    var inner: FuelBurnSummaryStore,
    var cache: TtlCache<FuelBurnSummary>,
    var hits: Int,
    var misses: Int,
) : FuelBurnSummaryStore {
    override fun find(code: String): FuelBurnSummary? {
        val hit = (cache.get(code) as FuelBurnSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnSummary> = inner.list()
}

fun newCachedFuelBurnSummaryStore(inner: FuelBurnSummaryStore, ttl: Int, clock: Clock): FuelBurnSummaryStore {
    return CachedFuelBurnSummaryStore(
        inner = inner,
        cache = newTtlCache<FuelBurnSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn summaries. Empty fields match all. */
data class FuelBurnSummaryQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnSummary>): MutableList<FuelBurnSummary> {
        val matched = filter(items) { candidate: FuelBurnSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnSummaries(store: FuelBurnSummaryStore, query: FuelBurnSummaryQuery): MutableList<FuelBurnSummary> {
    return query.apply(store.list())
}
