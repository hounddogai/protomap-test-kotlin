package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection adjustments by code. */
interface InspectionAdjustmentStore {
    fun find(code: String): InspectionAdjustment?
    fun save(item: InspectionAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionAdjustmentStore(
    var items: MutableMap<String, InspectionAdjustment>,
    var order: MutableList<String>,
) : InspectionAdjustmentStore {
    override fun find(code: String): InspectionAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionAdjustment) {
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

    override fun list(): MutableList<InspectionAdjustment> {
        val out = mutableListOf<InspectionAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionAdjustmentStore(): InspectionAdjustmentStore {
    return MemoryInspectionAdjustmentStore(
        mutableMapOf<String, InspectionAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionAdjustmentStore(
    var inner: InspectionAdjustmentStore,
    var cache: TtlCache<InspectionAdjustment>,
    var hits: Int,
    var misses: Int,
) : InspectionAdjustmentStore {
    override fun find(code: String): InspectionAdjustment? {
        val hit = (cache.get(code) as InspectionAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionAdjustment> = inner.list()
}

fun newCachedInspectionAdjustmentStore(inner: InspectionAdjustmentStore, ttl: Int, clock: Clock): InspectionAdjustmentStore {
    return CachedInspectionAdjustmentStore(
        inner = inner,
        cache = newTtlCache<InspectionAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection adjustments. Empty fields match all. */
data class InspectionAdjustmentQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<InspectionAdjustment>): MutableList<InspectionAdjustment> {
        val matched = filter(items) { candidate: InspectionAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionAdjustments(store: InspectionAdjustmentStore, query: InspectionAdjustmentQuery): MutableList<InspectionAdjustment> {
    return query.apply(store.list())
}
