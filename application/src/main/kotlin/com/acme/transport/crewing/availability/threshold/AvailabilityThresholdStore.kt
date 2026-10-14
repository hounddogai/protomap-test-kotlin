package com.acme.transport.crewing.availability.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability thresholds by code. */
interface AvailabilityThresholdStore {
    fun find(code: String): AvailabilityThreshold?
    fun save(item: AvailabilityThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityThresholdStore(
    var items: MutableMap<String, AvailabilityThreshold>,
    var order: MutableList<String>,
) : AvailabilityThresholdStore {
    override fun find(code: String): AvailabilityThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityThreshold) {
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

    override fun list(): MutableList<AvailabilityThreshold> {
        val out = mutableListOf<AvailabilityThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityThresholdStore(): AvailabilityThresholdStore {
    return MemoryAvailabilityThresholdStore(
        mutableMapOf<String, AvailabilityThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityThresholdStore(
    var inner: AvailabilityThresholdStore,
    var cache: TtlCache<AvailabilityThreshold>,
    var hits: Int,
    var misses: Int,
) : AvailabilityThresholdStore {
    override fun find(code: String): AvailabilityThreshold? {
        val hit = (cache.get(code) as AvailabilityThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityThreshold> = inner.list()
}

fun newCachedAvailabilityThresholdStore(inner: AvailabilityThresholdStore, ttl: Int, clock: Clock): AvailabilityThresholdStore {
    return CachedAvailabilityThresholdStore(
        inner = inner,
        cache = newTtlCache<AvailabilityThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability thresholds. Empty fields match all. */
data class AvailabilityThresholdQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityThreshold>): MutableList<AvailabilityThreshold> {
        val matched = filter(items) { candidate: AvailabilityThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityThresholds(store: AvailabilityThresholdStore, query: AvailabilityThresholdQuery): MutableList<AvailabilityThreshold> {
    return query.apply(store.list())
}
