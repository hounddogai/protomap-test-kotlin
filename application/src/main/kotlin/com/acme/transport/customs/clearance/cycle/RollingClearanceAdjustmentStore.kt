package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling clearance adjustments by code. */
interface RollingClearanceAdjustmentStore {
    fun find(code: String): RollingClearanceAdjustment?
    fun save(item: RollingClearanceAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingClearanceAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingClearanceAdjustmentStore(
    var items: MutableMap<String, RollingClearanceAdjustment>,
    var order: MutableList<String>,
) : RollingClearanceAdjustmentStore {
    override fun find(code: String): RollingClearanceAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingClearanceAdjustment) {
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

    override fun list(): MutableList<RollingClearanceAdjustment> {
        val out = mutableListOf<RollingClearanceAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingClearanceAdjustmentStore(): RollingClearanceAdjustmentStore {
    return MemoryRollingClearanceAdjustmentStore(
        mutableMapOf<String, RollingClearanceAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingClearanceAdjustmentStore(
    var inner: RollingClearanceAdjustmentStore,
    var cache: TtlCache<RollingClearanceAdjustment>,
    var hits: Int,
    var misses: Int,
) : RollingClearanceAdjustmentStore {
    override fun find(code: String): RollingClearanceAdjustment? {
        val hit = (cache.get(code) as RollingClearanceAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingClearanceAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingClearanceAdjustment> = inner.list()
}

fun newCachedRollingClearanceAdjustmentStore(inner: RollingClearanceAdjustmentStore, ttl: Int, clock: Clock): RollingClearanceAdjustmentStore {
    return CachedRollingClearanceAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RollingClearanceAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling clearance adjustments. Empty fields match all. */
data class RollingClearanceAdjustmentQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingClearanceAdjustment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingClearanceAdjustment>): MutableList<RollingClearanceAdjustment> {
        val matched = filter(items) { candidate: RollingClearanceAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingClearanceAdjustments(store: RollingClearanceAdjustmentStore, query: RollingClearanceAdjustmentQuery): MutableList<RollingClearanceAdjustment> {
    return query.apply(store.list())
}
