package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection thresholds by code. */
interface InspectionThresholdStore {
    fun find(code: String): InspectionThreshold?
    fun save(item: InspectionThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionThresholdStore(
    var items: MutableMap<String, InspectionThreshold>,
    var order: MutableList<String>,
) : InspectionThresholdStore {
    override fun find(code: String): InspectionThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionThreshold) {
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

    override fun list(): MutableList<InspectionThreshold> {
        val out = mutableListOf<InspectionThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionThresholdStore(): InspectionThresholdStore {
    return MemoryInspectionThresholdStore(mutableMapOf<String, InspectionThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionThresholdStore(
    var inner: InspectionThresholdStore,
    var cache: TtlCache<InspectionThreshold>,
    var hits: Int,
    var misses: Int,
) : InspectionThresholdStore {
    override fun find(code: String): InspectionThreshold? {
        val hit = (cache.get(code) as InspectionThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionThreshold> = inner.list()
}

fun newCachedInspectionThresholdStore(inner: InspectionThresholdStore, ttl: Int, clock: Clock): InspectionThresholdStore {
    return CachedInspectionThresholdStore(
        inner = inner,
        cache = newTtlCache<InspectionThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection thresholds. Empty fields match all. */
data class InspectionThresholdQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<InspectionThreshold>): MutableList<InspectionThreshold> {
        val matched = filter(items) { candidate: InspectionThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionThresholds(store: InspectionThresholdStore, query: InspectionThresholdQuery): MutableList<InspectionThreshold> {
    return query.apply(store.list())
}
