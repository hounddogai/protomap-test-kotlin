package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance plans by code. */
interface ClearancePlanStore {
    fun find(code: String): ClearancePlan?
    fun save(item: ClearancePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearancePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearancePlanStore(
    var items: MutableMap<String, ClearancePlan>,
    var order: MutableList<String>,
) : ClearancePlanStore {
    override fun find(code: String): ClearancePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearancePlan) {
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

    override fun list(): MutableList<ClearancePlan> {
        val out = mutableListOf<ClearancePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearancePlanStore(): ClearancePlanStore {
    return MemoryClearancePlanStore(mutableMapOf<String, ClearancePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearancePlanStore(
    var inner: ClearancePlanStore,
    var cache: TtlCache<ClearancePlan>,
    var hits: Int,
    var misses: Int,
) : ClearancePlanStore {
    override fun find(code: String): ClearancePlan? {
        val hit = (cache.get(code) as ClearancePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearancePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearancePlan> = inner.list()
}

fun newCachedClearancePlanStore(inner: ClearancePlanStore, ttl: Int, clock: Clock): ClearancePlanStore {
    return CachedClearancePlanStore(
        inner = inner,
        cache = newTtlCache<ClearancePlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance plans. Empty fields match all. */
data class ClearancePlanQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearancePlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ClearancePlan>): MutableList<ClearancePlan> {
        val matched = filter(items) { candidate: ClearancePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearancePlans(store: ClearancePlanStore, query: ClearancePlanQuery): MutableList<ClearancePlan> {
    return query.apply(store.list())
}
