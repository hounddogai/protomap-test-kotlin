package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dock door profiles by code. */
interface WeightedDockDoorProfileStore {
    fun find(code: String): WeightedDockDoorProfile?
    fun save(item: WeightedDockDoorProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDockDoorProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDockDoorProfileStore(
    var items: MutableMap<String, WeightedDockDoorProfile>,
    var order: MutableList<String>,
) : WeightedDockDoorProfileStore {
    override fun find(code: String): WeightedDockDoorProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDockDoorProfile) {
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

    override fun list(): MutableList<WeightedDockDoorProfile> {
        val out = mutableListOf<WeightedDockDoorProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDockDoorProfileStore(): WeightedDockDoorProfileStore {
    return MemoryWeightedDockDoorProfileStore(
        mutableMapOf<String, WeightedDockDoorProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDockDoorProfileStore(
    var inner: WeightedDockDoorProfileStore,
    var cache: TtlCache<WeightedDockDoorProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedDockDoorProfileStore {
    override fun find(code: String): WeightedDockDoorProfile? {
        val hit = (cache.get(code) as WeightedDockDoorProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDockDoorProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDockDoorProfile> = inner.list()
}

fun newCachedWeightedDockDoorProfileStore(inner: WeightedDockDoorProfileStore, ttl: Int, clock: Clock): WeightedDockDoorProfileStore {
    return CachedWeightedDockDoorProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedDockDoorProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dock door profiles. Empty fields match all. */
data class WeightedDockDoorProfileQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDockDoorProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<WeightedDockDoorProfile>): MutableList<WeightedDockDoorProfile> {
        val matched = filter(items) { candidate: WeightedDockDoorProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDockDoorProfiles(store: WeightedDockDoorProfileStore, query: WeightedDockDoorProfileQuery): MutableList<WeightedDockDoorProfile> {
    return query.apply(store.list())
}
