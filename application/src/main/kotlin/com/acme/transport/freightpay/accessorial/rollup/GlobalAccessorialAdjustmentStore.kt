package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global accessorial adjustments by code. */
interface GlobalAccessorialAdjustmentStore {
    fun find(code: String): GlobalAccessorialAdjustment?
    fun save(item: GlobalAccessorialAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAccessorialAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAccessorialAdjustmentStore(
    var items: MutableMap<String, GlobalAccessorialAdjustment>,
    var order: MutableList<String>,
) : GlobalAccessorialAdjustmentStore {
    override fun find(code: String): GlobalAccessorialAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAccessorialAdjustment) {
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

    override fun list(): MutableList<GlobalAccessorialAdjustment> {
        val out = mutableListOf<GlobalAccessorialAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAccessorialAdjustmentStore(): GlobalAccessorialAdjustmentStore {
    return MemoryGlobalAccessorialAdjustmentStore(
        mutableMapOf<String, GlobalAccessorialAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAccessorialAdjustmentStore(
    var inner: GlobalAccessorialAdjustmentStore,
    var cache: TtlCache<GlobalAccessorialAdjustment>,
    var hits: Int,
    var misses: Int,
) : GlobalAccessorialAdjustmentStore {
    override fun find(code: String): GlobalAccessorialAdjustment? {
        val hit = (cache.get(code) as GlobalAccessorialAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAccessorialAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAccessorialAdjustment> = inner.list()
}

fun newCachedGlobalAccessorialAdjustmentStore(inner: GlobalAccessorialAdjustmentStore, ttl: Int, clock: Clock): GlobalAccessorialAdjustmentStore {
    return CachedGlobalAccessorialAdjustmentStore(
        inner = inner,
        cache = newTtlCache<GlobalAccessorialAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global accessorial adjustments. Empty fields match all. */
data class GlobalAccessorialAdjustmentQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAccessorialAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<GlobalAccessorialAdjustment>): MutableList<GlobalAccessorialAdjustment> {
        val matched = filter(items) { candidate: GlobalAccessorialAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAccessorialAdjustments(store: GlobalAccessorialAdjustmentStore, query: GlobalAccessorialAdjustmentQuery): MutableList<GlobalAccessorialAdjustment> {
    return query.apply(store.list())
}
