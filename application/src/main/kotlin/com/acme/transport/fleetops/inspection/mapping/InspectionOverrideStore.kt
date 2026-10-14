package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection overrides by code. */
interface InspectionOverrideStore {
    fun find(code: String): InspectionOverride?
    fun save(item: InspectionOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionOverrideStore(
    var items: MutableMap<String, InspectionOverride>,
    var order: MutableList<String>,
) : InspectionOverrideStore {
    override fun find(code: String): InspectionOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionOverride) {
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

    override fun list(): MutableList<InspectionOverride> {
        val out = mutableListOf<InspectionOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionOverrideStore(): InspectionOverrideStore {
    return MemoryInspectionOverrideStore(mutableMapOf<String, InspectionOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionOverrideStore(
    var inner: InspectionOverrideStore,
    var cache: TtlCache<InspectionOverride>,
    var hits: Int,
    var misses: Int,
) : InspectionOverrideStore {
    override fun find(code: String): InspectionOverride? {
        val hit = (cache.get(code) as InspectionOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionOverride> = inner.list()
}

fun newCachedInspectionOverrideStore(inner: InspectionOverrideStore, ttl: Int, clock: Clock): InspectionOverrideStore {
    return CachedInspectionOverrideStore(
        inner = inner,
        cache = newTtlCache<InspectionOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection overrides. Empty fields match all. */
data class InspectionOverrideQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<InspectionOverride>): MutableList<InspectionOverride> {
        val matched = filter(items) { candidate: InspectionOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionOverrides(store: InspectionOverrideStore, query: InspectionOverrideQuery): MutableList<InspectionOverride> {
    return query.apply(store.list())
}
