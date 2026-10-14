package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered near miss adjustments by code. */
interface TieredNearMissAdjustmentStore {
    fun find(code: String): TieredNearMissAdjustment?
    fun save(item: TieredNearMissAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredNearMissAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredNearMissAdjustmentStore(
    var items: MutableMap<String, TieredNearMissAdjustment>,
    var order: MutableList<String>,
) : TieredNearMissAdjustmentStore {
    override fun find(code: String): TieredNearMissAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredNearMissAdjustment) {
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

    override fun list(): MutableList<TieredNearMissAdjustment> {
        val out = mutableListOf<TieredNearMissAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredNearMissAdjustmentStore(): TieredNearMissAdjustmentStore {
    return MemoryTieredNearMissAdjustmentStore(
        mutableMapOf<String, TieredNearMissAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredNearMissAdjustmentStore(
    var inner: TieredNearMissAdjustmentStore,
    var cache: TtlCache<TieredNearMissAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredNearMissAdjustmentStore {
    override fun find(code: String): TieredNearMissAdjustment? {
        val hit = (cache.get(code) as TieredNearMissAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredNearMissAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredNearMissAdjustment> = inner.list()
}

fun newCachedTieredNearMissAdjustmentStore(inner: TieredNearMissAdjustmentStore, ttl: Int, clock: Clock): TieredNearMissAdjustmentStore {
    return CachedTieredNearMissAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredNearMissAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered near miss adjustments. Empty fields match all. */
data class TieredNearMissAdjustmentQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredNearMissAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TieredNearMissAdjustment>): MutableList<TieredNearMissAdjustment> {
        val matched = filter(items) { candidate: TieredNearMissAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredNearMissAdjustments(store: TieredNearMissAdjustmentStore, query: TieredNearMissAdjustmentQuery): MutableList<TieredNearMissAdjustment> {
    return query.apply(store.list())
}
