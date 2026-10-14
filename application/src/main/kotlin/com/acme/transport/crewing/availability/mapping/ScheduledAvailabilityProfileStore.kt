package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled availability profiles by code. */
interface ScheduledAvailabilityProfileStore {
    fun find(code: String): ScheduledAvailabilityProfile?
    fun save(item: ScheduledAvailabilityProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAvailabilityProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAvailabilityProfileStore(
    var items: MutableMap<String, ScheduledAvailabilityProfile>,
    var order: MutableList<String>,
) : ScheduledAvailabilityProfileStore {
    override fun find(code: String): ScheduledAvailabilityProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAvailabilityProfile) {
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

    override fun list(): MutableList<ScheduledAvailabilityProfile> {
        val out = mutableListOf<ScheduledAvailabilityProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAvailabilityProfileStore(): ScheduledAvailabilityProfileStore {
    return MemoryScheduledAvailabilityProfileStore(
        mutableMapOf<String, ScheduledAvailabilityProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAvailabilityProfileStore(
    var inner: ScheduledAvailabilityProfileStore,
    var cache: TtlCache<ScheduledAvailabilityProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledAvailabilityProfileStore {
    override fun find(code: String): ScheduledAvailabilityProfile? {
        val hit = (cache.get(code) as ScheduledAvailabilityProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAvailabilityProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAvailabilityProfile> = inner.list()
}

fun newCachedScheduledAvailabilityProfileStore(inner: ScheduledAvailabilityProfileStore, ttl: Int, clock: Clock): ScheduledAvailabilityProfileStore {
    return CachedScheduledAvailabilityProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledAvailabilityProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled availability profiles. Empty fields match all. */
data class ScheduledAvailabilityProfileQuery(
    var region: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAvailabilityProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAvailabilityProfile>): MutableList<ScheduledAvailabilityProfile> {
        val matched = filter(items) { candidate: ScheduledAvailabilityProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAvailabilityProfiles(store: ScheduledAvailabilityProfileStore, query: ScheduledAvailabilityProfileQuery): MutableList<ScheduledAvailabilityProfile> {
    return query.apply(store.list())
}
