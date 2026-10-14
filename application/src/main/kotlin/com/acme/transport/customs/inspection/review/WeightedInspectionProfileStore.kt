package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection profiles by code. */
interface WeightedInspectionProfileStore {
    fun find(code: String): WeightedInspectionProfile?
    fun save(item: WeightedInspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionProfileStore(
    var items: MutableMap<String, WeightedInspectionProfile>,
    var order: MutableList<String>,
) : WeightedInspectionProfileStore {
    override fun find(code: String): WeightedInspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionProfile) {
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

    override fun list(): MutableList<WeightedInspectionProfile> {
        val out = mutableListOf<WeightedInspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionProfileStore(): WeightedInspectionProfileStore {
    return MemoryWeightedInspectionProfileStore(
        mutableMapOf<String, WeightedInspectionProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionProfileStore(
    var inner: WeightedInspectionProfileStore,
    var cache: TtlCache<WeightedInspectionProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionProfileStore {
    override fun find(code: String): WeightedInspectionProfile? {
        val hit = (cache.get(code) as WeightedInspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionProfile> = inner.list()
}

fun newCachedWeightedInspectionProfileStore(inner: WeightedInspectionProfileStore, ttl: Int, clock: Clock): WeightedInspectionProfileStore {
    return CachedWeightedInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection profiles. Empty fields match all. */
data class WeightedInspectionProfileQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionProfile>): MutableList<WeightedInspectionProfile> {
        val matched = filter(items) { candidate: WeightedInspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionProfiles(store: WeightedInspectionProfileStore, query: WeightedInspectionProfileQuery): MutableList<WeightedInspectionProfile> {
    return query.apply(store.list())
}
