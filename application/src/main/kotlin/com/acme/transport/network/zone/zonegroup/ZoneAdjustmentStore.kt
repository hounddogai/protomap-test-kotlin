package com.acme.transport.network.zone.zonegroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone adjustments by code. */
interface ZoneAdjustmentStore {
    fun find(code: String): ZoneAdjustment?
    fun save(item: ZoneAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneAdjustmentStore(
    var items: MutableMap<String, ZoneAdjustment>,
    var order: MutableList<String>,
) : ZoneAdjustmentStore {
    override fun find(code: String): ZoneAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneAdjustment) {
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

    override fun list(): MutableList<ZoneAdjustment> {
        val out = mutableListOf<ZoneAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneAdjustmentStore(): ZoneAdjustmentStore {
    return MemoryZoneAdjustmentStore(mutableMapOf<String, ZoneAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneAdjustmentStore(
    var inner: ZoneAdjustmentStore,
    var cache: TtlCache<ZoneAdjustment>,
    var hits: Int,
    var misses: Int,
) : ZoneAdjustmentStore {
    override fun find(code: String): ZoneAdjustment? {
        val hit = (cache.get(code) as ZoneAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneAdjustment> = inner.list()
}

fun newCachedZoneAdjustmentStore(inner: ZoneAdjustmentStore, ttl: Int, clock: Clock): ZoneAdjustmentStore {
    return CachedZoneAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ZoneAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone adjustments. Empty fields match all. */
data class ZoneAdjustmentQuery(
    var lane: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneAdjustment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ZoneAdjustment>): MutableList<ZoneAdjustment> {
        val matched = filter(items) { candidate: ZoneAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneAdjustments(store: ZoneAdjustmentStore, query: ZoneAdjustmentQuery): MutableList<ZoneAdjustment> {
    return query.apply(store.list())
}
