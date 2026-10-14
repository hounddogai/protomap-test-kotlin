package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability profiles by code. */
interface AvailabilityProfileStore {
    fun find(code: String): AvailabilityProfile?
    fun save(item: AvailabilityProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityProfileStore(
    var items: MutableMap<String, AvailabilityProfile>,
    var order: MutableList<String>,
) : AvailabilityProfileStore {
    override fun find(code: String): AvailabilityProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityProfile) {
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

    override fun list(): MutableList<AvailabilityProfile> {
        val out = mutableListOf<AvailabilityProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityProfileStore(): AvailabilityProfileStore {
    return MemoryAvailabilityProfileStore(mutableMapOf<String, AvailabilityProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityProfileStore(
    var inner: AvailabilityProfileStore,
    var cache: TtlCache<AvailabilityProfile>,
    var hits: Int,
    var misses: Int,
) : AvailabilityProfileStore {
    override fun find(code: String): AvailabilityProfile? {
        val hit = (cache.get(code) as AvailabilityProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityProfile> = inner.list()
}

fun newCachedAvailabilityProfileStore(inner: AvailabilityProfileStore, ttl: Int, clock: Clock): AvailabilityProfileStore {
    return CachedAvailabilityProfileStore(
        inner = inner,
        cache = newTtlCache<AvailabilityProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability profiles. Empty fields match all. */
data class AvailabilityProfileQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AvailabilityProfile>): MutableList<AvailabilityProfile> {
        val matched = filter(items) { candidate: AvailabilityProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityProfiles(store: AvailabilityProfileStore, query: AvailabilityProfileQuery): MutableList<AvailabilityProfile> {
    return query.apply(store.list())
}
