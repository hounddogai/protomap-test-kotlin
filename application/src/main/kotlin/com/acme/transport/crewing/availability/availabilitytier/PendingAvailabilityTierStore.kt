package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending availability tiers by code. */
interface PendingAvailabilityTierStore {
    fun find(code: String): PendingAvailabilityTier?
    fun save(item: PendingAvailabilityTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAvailabilityTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAvailabilityTierStore(
    var items: MutableMap<String, PendingAvailabilityTier>,
    var order: MutableList<String>,
) : PendingAvailabilityTierStore {
    override fun find(code: String): PendingAvailabilityTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAvailabilityTier) {
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

    override fun list(): MutableList<PendingAvailabilityTier> {
        val out = mutableListOf<PendingAvailabilityTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAvailabilityTierStore(): PendingAvailabilityTierStore {
    return MemoryPendingAvailabilityTierStore(
        mutableMapOf<String, PendingAvailabilityTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAvailabilityTierStore(
    var inner: PendingAvailabilityTierStore,
    var cache: TtlCache<PendingAvailabilityTier>,
    var hits: Int,
    var misses: Int,
) : PendingAvailabilityTierStore {
    override fun find(code: String): PendingAvailabilityTier? {
        val hit = (cache.get(code) as PendingAvailabilityTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAvailabilityTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAvailabilityTier> = inner.list()
}

fun newCachedPendingAvailabilityTierStore(inner: PendingAvailabilityTierStore, ttl: Int, clock: Clock): PendingAvailabilityTierStore {
    return CachedPendingAvailabilityTierStore(
        inner = inner,
        cache = newTtlCache<PendingAvailabilityTier>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending availability tiers. Empty fields match all. */
data class PendingAvailabilityTierQuery(
    var region: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAvailabilityTier): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAvailabilityTier>): MutableList<PendingAvailabilityTier> {
        val matched = filter(items) { candidate: PendingAvailabilityTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAvailabilityTiers(store: PendingAvailabilityTierStore, query: PendingAvailabilityTierQuery): MutableList<PendingAvailabilityTier> {
    return query.apply(store.list())
}
