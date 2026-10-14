package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary service plan profiles by code. */
interface PrimaryServicePlanProfileStore {
    fun find(code: String): PrimaryServicePlanProfile?
    fun save(item: PrimaryServicePlanProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServicePlanProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServicePlanProfileStore(
    var items: MutableMap<String, PrimaryServicePlanProfile>,
    var order: MutableList<String>,
) : PrimaryServicePlanProfileStore {
    override fun find(code: String): PrimaryServicePlanProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServicePlanProfile) {
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

    override fun list(): MutableList<PrimaryServicePlanProfile> {
        val out = mutableListOf<PrimaryServicePlanProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServicePlanProfileStore(): PrimaryServicePlanProfileStore {
    return MemoryPrimaryServicePlanProfileStore(
        mutableMapOf<String, PrimaryServicePlanProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServicePlanProfileStore(
    var inner: PrimaryServicePlanProfileStore,
    var cache: TtlCache<PrimaryServicePlanProfile>,
    var hits: Int,
    var misses: Int,
) : PrimaryServicePlanProfileStore {
    override fun find(code: String): PrimaryServicePlanProfile? {
        val hit = (cache.get(code) as PrimaryServicePlanProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServicePlanProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServicePlanProfile> = inner.list()
}

fun newCachedPrimaryServicePlanProfileStore(inner: PrimaryServicePlanProfileStore, ttl: Int, clock: Clock): PrimaryServicePlanProfileStore {
    return CachedPrimaryServicePlanProfileStore(
        inner = inner,
        cache = newTtlCache<PrimaryServicePlanProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service plan profiles. Empty fields match all. */
data class PrimaryServicePlanProfileQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServicePlanProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryServicePlanProfile>): MutableList<PrimaryServicePlanProfile> {
        val matched = filter(items) { candidate: PrimaryServicePlanProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServicePlanProfiles(store: PrimaryServicePlanProfileStore, query: PrimaryServicePlanProfileQuery): MutableList<PrimaryServicePlanProfile> {
    return query.apply(store.list())
}
