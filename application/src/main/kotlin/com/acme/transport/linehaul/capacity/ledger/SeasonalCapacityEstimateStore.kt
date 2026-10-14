package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal capacity estimates by code. */
interface SeasonalCapacityEstimateStore {
    fun find(code: String): SeasonalCapacityEstimate?
    fun save(item: SeasonalCapacityEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCapacityEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCapacityEstimateStore(
    var items: MutableMap<String, SeasonalCapacityEstimate>,
    var order: MutableList<String>,
) : SeasonalCapacityEstimateStore {
    override fun find(code: String): SeasonalCapacityEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCapacityEstimate) {
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

    override fun list(): MutableList<SeasonalCapacityEstimate> {
        val out = mutableListOf<SeasonalCapacityEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCapacityEstimateStore(): SeasonalCapacityEstimateStore {
    return MemorySeasonalCapacityEstimateStore(
        mutableMapOf<String, SeasonalCapacityEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCapacityEstimateStore(
    var inner: SeasonalCapacityEstimateStore,
    var cache: TtlCache<SeasonalCapacityEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalCapacityEstimateStore {
    override fun find(code: String): SeasonalCapacityEstimate? {
        val hit = (cache.get(code) as SeasonalCapacityEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCapacityEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCapacityEstimate> = inner.list()
}

fun newCachedSeasonalCapacityEstimateStore(inner: SeasonalCapacityEstimateStore, ttl: Int, clock: Clock): SeasonalCapacityEstimateStore {
    return CachedSeasonalCapacityEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalCapacityEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal capacity estimates. Empty fields match all. */
data class SeasonalCapacityEstimateQuery(
    var region: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCapacityEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCapacityEstimate>): MutableList<SeasonalCapacityEstimate> {
        val matched = filter(items) { candidate: SeasonalCapacityEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCapacityEstimates(store: SeasonalCapacityEstimateStore, query: SeasonalCapacityEstimateQuery): MutableList<SeasonalCapacityEstimate> {
    return query.apply(store.list())
}
