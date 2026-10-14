package com.acme.transport.network.zone.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted zone profiles by code. */
interface WeightedZoneProfileStore {
    fun find(code: String): WeightedZoneProfile?
    fun save(item: WeightedZoneProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedZoneProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedZoneProfileStore(
    var items: MutableMap<String, WeightedZoneProfile>,
    var order: MutableList<String>,
) : WeightedZoneProfileStore {
    override fun find(code: String): WeightedZoneProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedZoneProfile) {
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

    override fun list(): MutableList<WeightedZoneProfile> {
        val out = mutableListOf<WeightedZoneProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedZoneProfileStore(): WeightedZoneProfileStore {
    return MemoryWeightedZoneProfileStore(mutableMapOf<String, WeightedZoneProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedZoneProfileStore(
    var inner: WeightedZoneProfileStore,
    var cache: TtlCache<WeightedZoneProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedZoneProfileStore {
    override fun find(code: String): WeightedZoneProfile? {
        val hit = (cache.get(code) as WeightedZoneProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedZoneProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedZoneProfile> = inner.list()
}

fun newCachedWeightedZoneProfileStore(inner: WeightedZoneProfileStore, ttl: Int, clock: Clock): WeightedZoneProfileStore {
    return CachedWeightedZoneProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedZoneProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted zone profiles. Empty fields match all. */
data class WeightedZoneProfileQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedZoneProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedZoneProfile>): MutableList<WeightedZoneProfile> {
        val matched = filter(items) { candidate: WeightedZoneProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedZoneProfiles(store: WeightedZoneProfileStore, query: WeightedZoneProfileQuery): MutableList<WeightedZoneProfile> {
    return query.apply(store.list())
}
