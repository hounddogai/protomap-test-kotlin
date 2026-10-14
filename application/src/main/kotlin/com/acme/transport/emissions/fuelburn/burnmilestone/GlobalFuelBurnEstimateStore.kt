package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel burn estimates by code. */
interface GlobalFuelBurnEstimateStore {
    fun find(code: String): GlobalFuelBurnEstimate?
    fun save(item: GlobalFuelBurnEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelBurnEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelBurnEstimateStore(
    var items: MutableMap<String, GlobalFuelBurnEstimate>,
    var order: MutableList<String>,
) : GlobalFuelBurnEstimateStore {
    override fun find(code: String): GlobalFuelBurnEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelBurnEstimate) {
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

    override fun list(): MutableList<GlobalFuelBurnEstimate> {
        val out = mutableListOf<GlobalFuelBurnEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelBurnEstimateStore(): GlobalFuelBurnEstimateStore {
    return MemoryGlobalFuelBurnEstimateStore(
        mutableMapOf<String, GlobalFuelBurnEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelBurnEstimateStore(
    var inner: GlobalFuelBurnEstimateStore,
    var cache: TtlCache<GlobalFuelBurnEstimate>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelBurnEstimateStore {
    override fun find(code: String): GlobalFuelBurnEstimate? {
        val hit = (cache.get(code) as GlobalFuelBurnEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelBurnEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelBurnEstimate> = inner.list()
}

fun newCachedGlobalFuelBurnEstimateStore(inner: GlobalFuelBurnEstimateStore, ttl: Int, clock: Clock): GlobalFuelBurnEstimateStore {
    return CachedGlobalFuelBurnEstimateStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelBurnEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel burn estimates. Empty fields match all. */
data class GlobalFuelBurnEstimateQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelBurnEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelBurnEstimate>): MutableList<GlobalFuelBurnEstimate> {
        val matched = filter(items) { candidate: GlobalFuelBurnEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelBurnEstimates(store: GlobalFuelBurnEstimateStore, query: GlobalFuelBurnEstimateQuery): MutableList<GlobalFuelBurnEstimate> {
    return query.apply(store.list())
}
