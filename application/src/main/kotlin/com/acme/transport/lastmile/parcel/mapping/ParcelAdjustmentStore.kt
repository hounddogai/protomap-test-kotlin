package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel adjustments by code. */
interface ParcelAdjustmentStore {
    fun find(code: String): ParcelAdjustment?
    fun save(item: ParcelAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelAdjustmentStore(
    var items: MutableMap<String, ParcelAdjustment>,
    var order: MutableList<String>,
) : ParcelAdjustmentStore {
    override fun find(code: String): ParcelAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelAdjustment) {
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

    override fun list(): MutableList<ParcelAdjustment> {
        val out = mutableListOf<ParcelAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelAdjustmentStore(): ParcelAdjustmentStore {
    return MemoryParcelAdjustmentStore(mutableMapOf<String, ParcelAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelAdjustmentStore(
    var inner: ParcelAdjustmentStore,
    var cache: TtlCache<ParcelAdjustment>,
    var hits: Int,
    var misses: Int,
) : ParcelAdjustmentStore {
    override fun find(code: String): ParcelAdjustment? {
        val hit = (cache.get(code) as ParcelAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelAdjustment> = inner.list()
}

fun newCachedParcelAdjustmentStore(inner: ParcelAdjustmentStore, ttl: Int, clock: Clock): ParcelAdjustmentStore {
    return CachedParcelAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ParcelAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel adjustments. Empty fields match all. */
data class ParcelAdjustmentQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelAdjustment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelAdjustment>): MutableList<ParcelAdjustment> {
        val matched = filter(items) { candidate: ParcelAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelAdjustments(store: ParcelAdjustmentStore, query: ParcelAdjustmentQuery): MutableList<ParcelAdjustment> {
    return query.apply(store.list())
}
