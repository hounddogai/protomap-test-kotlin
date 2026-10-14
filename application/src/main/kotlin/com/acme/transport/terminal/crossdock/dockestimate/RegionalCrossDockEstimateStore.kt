package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional cross dock estimates by code. */
interface RegionalCrossDockEstimateStore {
    fun find(code: String): RegionalCrossDockEstimate?
    fun save(item: RegionalCrossDockEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCrossDockEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCrossDockEstimateStore(
    var items: MutableMap<String, RegionalCrossDockEstimate>,
    var order: MutableList<String>,
) : RegionalCrossDockEstimateStore {
    override fun find(code: String): RegionalCrossDockEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCrossDockEstimate) {
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

    override fun list(): MutableList<RegionalCrossDockEstimate> {
        val out = mutableListOf<RegionalCrossDockEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCrossDockEstimateStore(): RegionalCrossDockEstimateStore {
    return MemoryRegionalCrossDockEstimateStore(
        mutableMapOf<String, RegionalCrossDockEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCrossDockEstimateStore(
    var inner: RegionalCrossDockEstimateStore,
    var cache: TtlCache<RegionalCrossDockEstimate>,
    var hits: Int,
    var misses: Int,
) : RegionalCrossDockEstimateStore {
    override fun find(code: String): RegionalCrossDockEstimate? {
        val hit = (cache.get(code) as RegionalCrossDockEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCrossDockEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCrossDockEstimate> = inner.list()
}

fun newCachedRegionalCrossDockEstimateStore(inner: RegionalCrossDockEstimateStore, ttl: Int, clock: Clock): RegionalCrossDockEstimateStore {
    return CachedRegionalCrossDockEstimateStore(
        inner = inner,
        cache = newTtlCache<RegionalCrossDockEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional cross dock estimates. Empty fields match all. */
data class RegionalCrossDockEstimateQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCrossDockEstimate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RegionalCrossDockEstimate>): MutableList<RegionalCrossDockEstimate> {
        val matched = filter(items) { candidate: RegionalCrossDockEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCrossDockEstimates(store: RegionalCrossDockEstimateStore, query: RegionalCrossDockEstimateQuery): MutableList<RegionalCrossDockEstimate> {
    return query.apply(store.list())
}
