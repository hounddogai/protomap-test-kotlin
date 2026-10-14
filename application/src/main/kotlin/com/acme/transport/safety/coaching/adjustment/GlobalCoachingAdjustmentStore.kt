package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching adjustments by code. */
interface GlobalCoachingAdjustmentStore {
    fun find(code: String): GlobalCoachingAdjustment?
    fun save(item: GlobalCoachingAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingAdjustmentStore(
    var items: MutableMap<String, GlobalCoachingAdjustment>,
    var order: MutableList<String>,
) : GlobalCoachingAdjustmentStore {
    override fun find(code: String): GlobalCoachingAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingAdjustment) {
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

    override fun list(): MutableList<GlobalCoachingAdjustment> {
        val out = mutableListOf<GlobalCoachingAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingAdjustmentStore(): GlobalCoachingAdjustmentStore {
    return MemoryGlobalCoachingAdjustmentStore(
        mutableMapOf<String, GlobalCoachingAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingAdjustmentStore(
    var inner: GlobalCoachingAdjustmentStore,
    var cache: TtlCache<GlobalCoachingAdjustment>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingAdjustmentStore {
    override fun find(code: String): GlobalCoachingAdjustment? {
        val hit = (cache.get(code) as GlobalCoachingAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingAdjustment> = inner.list()
}

fun newCachedGlobalCoachingAdjustmentStore(inner: GlobalCoachingAdjustmentStore, ttl: Int, clock: Clock): GlobalCoachingAdjustmentStore {
    return CachedGlobalCoachingAdjustmentStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching adjustments. Empty fields match all. */
data class GlobalCoachingAdjustmentQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingAdjustment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<GlobalCoachingAdjustment>): MutableList<GlobalCoachingAdjustment> {
        val matched = filter(items) { candidate: GlobalCoachingAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingAdjustments(store: GlobalCoachingAdjustmentStore, query: GlobalCoachingAdjustmentQuery): MutableList<GlobalCoachingAdjustment> {
    return query.apply(store.list())
}
