package com.acme.transport.customs.inspection.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection profiles by code. */
interface InspectionProfileStore {
    fun find(code: String): InspectionProfile?
    fun save(item: InspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionProfileStore(
    var items: MutableMap<String, InspectionProfile>,
    var order: MutableList<String>,
) : InspectionProfileStore {
    override fun find(code: String): InspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionProfile) {
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

    override fun list(): MutableList<InspectionProfile> {
        val out = mutableListOf<InspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionProfileStore(): InspectionProfileStore {
    return MemoryInspectionProfileStore(mutableMapOf<String, InspectionProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionProfileStore(
    var inner: InspectionProfileStore,
    var cache: TtlCache<InspectionProfile>,
    var hits: Int,
    var misses: Int,
) : InspectionProfileStore {
    override fun find(code: String): InspectionProfile? {
        val hit = (cache.get(code) as InspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionProfile> = inner.list()
}

fun newCachedInspectionProfileStore(inner: InspectionProfileStore, ttl: Int, clock: Clock): InspectionProfileStore {
    return CachedInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<InspectionProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection profiles. Empty fields match all. */
data class InspectionProfileQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<InspectionProfile>): MutableList<InspectionProfile> {
        val matched = filter(items) { candidate: InspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionProfiles(store: InspectionProfileStore, query: InspectionProfileQuery): MutableList<InspectionProfile> {
    return query.apply(store.list())
}
