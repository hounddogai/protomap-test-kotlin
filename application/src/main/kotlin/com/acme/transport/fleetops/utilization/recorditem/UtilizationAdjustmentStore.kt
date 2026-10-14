package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization adjustments by code. */
interface UtilizationAdjustmentStore {
    fun find(code: String): UtilizationAdjustment?
    fun save(item: UtilizationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationAdjustmentStore(
    var items: MutableMap<String, UtilizationAdjustment>,
    var order: MutableList<String>,
) : UtilizationAdjustmentStore {
    override fun find(code: String): UtilizationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationAdjustment) {
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

    override fun list(): MutableList<UtilizationAdjustment> {
        val out = mutableListOf<UtilizationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationAdjustmentStore(): UtilizationAdjustmentStore {
    return MemoryUtilizationAdjustmentStore(
        mutableMapOf<String, UtilizationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationAdjustmentStore(
    var inner: UtilizationAdjustmentStore,
    var cache: TtlCache<UtilizationAdjustment>,
    var hits: Int,
    var misses: Int,
) : UtilizationAdjustmentStore {
    override fun find(code: String): UtilizationAdjustment? {
        val hit = (cache.get(code) as UtilizationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationAdjustment> = inner.list()
}

fun newCachedUtilizationAdjustmentStore(inner: UtilizationAdjustmentStore, ttl: Int, clock: Clock): UtilizationAdjustmentStore {
    return CachedUtilizationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<UtilizationAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization adjustments. Empty fields match all. */
data class UtilizationAdjustmentQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationAdjustment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationAdjustment>): MutableList<UtilizationAdjustment> {
        val matched = filter(items) { candidate: UtilizationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationAdjustments(store: UtilizationAdjustmentStore, query: UtilizationAdjustmentQuery): MutableList<UtilizationAdjustment> {
    return query.apply(store.list())
}
