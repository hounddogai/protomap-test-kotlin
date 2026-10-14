package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area estimates by code. */
interface RegionalServiceAreaEstimateStore {
    fun find(code: String): RegionalServiceAreaEstimate?
    fun save(item: RegionalServiceAreaEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaEstimateStore(
    var items: MutableMap<String, RegionalServiceAreaEstimate>,
    var order: MutableList<String>,
) : RegionalServiceAreaEstimateStore {
    override fun find(code: String): RegionalServiceAreaEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaEstimate) {
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

    override fun list(): MutableList<RegionalServiceAreaEstimate> {
        val out = mutableListOf<RegionalServiceAreaEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaEstimateStore(): RegionalServiceAreaEstimateStore {
    return MemoryRegionalServiceAreaEstimateStore(
        mutableMapOf<String, RegionalServiceAreaEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaEstimateStore(
    var inner: RegionalServiceAreaEstimateStore,
    var cache: TtlCache<RegionalServiceAreaEstimate>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaEstimateStore {
    override fun find(code: String): RegionalServiceAreaEstimate? {
        val hit = (cache.get(code) as RegionalServiceAreaEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaEstimate> = inner.list()
}

fun newCachedRegionalServiceAreaEstimateStore(inner: RegionalServiceAreaEstimateStore, ttl: Int, clock: Clock): RegionalServiceAreaEstimateStore {
    return CachedRegionalServiceAreaEstimateStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area estimates. Empty fields match all. */
data class RegionalServiceAreaEstimateQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaEstimate>): MutableList<RegionalServiceAreaEstimate> {
        val matched = filter(items) { candidate: RegionalServiceAreaEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaEstimates(store: RegionalServiceAreaEstimateStore, query: RegionalServiceAreaEstimateQuery): MutableList<RegionalServiceAreaEstimate> {
    return query.apply(store.list())
}
