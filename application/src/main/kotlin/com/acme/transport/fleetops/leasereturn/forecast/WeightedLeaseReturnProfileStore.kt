package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted lease return profiles by code. */
interface WeightedLeaseReturnProfileStore {
    fun find(code: String): WeightedLeaseReturnProfile?
    fun save(item: WeightedLeaseReturnProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLeaseReturnProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLeaseReturnProfileStore(
    var items: MutableMap<String, WeightedLeaseReturnProfile>,
    var order: MutableList<String>,
) : WeightedLeaseReturnProfileStore {
    override fun find(code: String): WeightedLeaseReturnProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLeaseReturnProfile) {
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

    override fun list(): MutableList<WeightedLeaseReturnProfile> {
        val out = mutableListOf<WeightedLeaseReturnProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLeaseReturnProfileStore(): WeightedLeaseReturnProfileStore {
    return MemoryWeightedLeaseReturnProfileStore(
        mutableMapOf<String, WeightedLeaseReturnProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLeaseReturnProfileStore(
    var inner: WeightedLeaseReturnProfileStore,
    var cache: TtlCache<WeightedLeaseReturnProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedLeaseReturnProfileStore {
    override fun find(code: String): WeightedLeaseReturnProfile? {
        val hit = (cache.get(code) as WeightedLeaseReturnProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLeaseReturnProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLeaseReturnProfile> = inner.list()
}

fun newCachedWeightedLeaseReturnProfileStore(inner: WeightedLeaseReturnProfileStore, ttl: Int, clock: Clock): WeightedLeaseReturnProfileStore {
    return CachedWeightedLeaseReturnProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedLeaseReturnProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lease return profiles. Empty fields match all. */
data class WeightedLeaseReturnProfileQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLeaseReturnProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLeaseReturnProfile>): MutableList<WeightedLeaseReturnProfile> {
        val matched = filter(items) { candidate: WeightedLeaseReturnProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLeaseReturnProfiles(store: WeightedLeaseReturnProfileStore, query: WeightedLeaseReturnProfileQuery): MutableList<WeightedLeaseReturnProfile> {
    return query.apply(store.list())
}
