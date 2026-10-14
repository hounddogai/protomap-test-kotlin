package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict inspection profiles by code. */
interface StrictInspectionProfileStore {
    fun find(code: String): StrictInspectionProfile?
    fun save(item: StrictInspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictInspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictInspectionProfileStore(
    var items: MutableMap<String, StrictInspectionProfile>,
    var order: MutableList<String>,
) : StrictInspectionProfileStore {
    override fun find(code: String): StrictInspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictInspectionProfile) {
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

    override fun list(): MutableList<StrictInspectionProfile> {
        val out = mutableListOf<StrictInspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictInspectionProfileStore(): StrictInspectionProfileStore {
    return MemoryStrictInspectionProfileStore(
        mutableMapOf<String, StrictInspectionProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictInspectionProfileStore(
    var inner: StrictInspectionProfileStore,
    var cache: TtlCache<StrictInspectionProfile>,
    var hits: Int,
    var misses: Int,
) : StrictInspectionProfileStore {
    override fun find(code: String): StrictInspectionProfile? {
        val hit = (cache.get(code) as StrictInspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictInspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictInspectionProfile> = inner.list()
}

fun newCachedStrictInspectionProfileStore(inner: StrictInspectionProfileStore, ttl: Int, clock: Clock): StrictInspectionProfileStore {
    return CachedStrictInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<StrictInspectionProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict inspection profiles. Empty fields match all. */
data class StrictInspectionProfileQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictInspectionProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<StrictInspectionProfile>): MutableList<StrictInspectionProfile> {
        val matched = filter(items) { candidate: StrictInspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictInspectionProfiles(store: StrictInspectionProfileStore, query: StrictInspectionProfileQuery): MutableList<StrictInspectionProfile> {
    return query.apply(store.list())
}
