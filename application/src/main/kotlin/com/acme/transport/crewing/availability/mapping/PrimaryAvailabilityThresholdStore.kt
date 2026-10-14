package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary availability thresholds by code. */
interface PrimaryAvailabilityThresholdStore {
    fun find(code: String): PrimaryAvailabilityThreshold?
    fun save(item: PrimaryAvailabilityThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAvailabilityThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAvailabilityThresholdStore(
    var items: MutableMap<String, PrimaryAvailabilityThreshold>,
    var order: MutableList<String>,
) : PrimaryAvailabilityThresholdStore {
    override fun find(code: String): PrimaryAvailabilityThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAvailabilityThreshold) {
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

    override fun list(): MutableList<PrimaryAvailabilityThreshold> {
        val out = mutableListOf<PrimaryAvailabilityThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAvailabilityThresholdStore(): PrimaryAvailabilityThresholdStore {
    return MemoryPrimaryAvailabilityThresholdStore(
        mutableMapOf<String, PrimaryAvailabilityThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAvailabilityThresholdStore(
    var inner: PrimaryAvailabilityThresholdStore,
    var cache: TtlCache<PrimaryAvailabilityThreshold>,
    var hits: Int,
    var misses: Int,
) : PrimaryAvailabilityThresholdStore {
    override fun find(code: String): PrimaryAvailabilityThreshold? {
        val hit = (cache.get(code) as PrimaryAvailabilityThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAvailabilityThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAvailabilityThreshold> = inner.list()
}

fun newCachedPrimaryAvailabilityThresholdStore(inner: PrimaryAvailabilityThresholdStore, ttl: Int, clock: Clock): PrimaryAvailabilityThresholdStore {
    return CachedPrimaryAvailabilityThresholdStore(
        inner = inner,
        cache = newTtlCache<PrimaryAvailabilityThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary availability thresholds. Empty fields match all. */
data class PrimaryAvailabilityThresholdQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAvailabilityThreshold): Boolean {
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

    fun apply(items: MutableList<PrimaryAvailabilityThreshold>): MutableList<PrimaryAvailabilityThreshold> {
        val matched = filter(items) { candidate: PrimaryAvailabilityThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAvailabilityThresholds(store: PrimaryAvailabilityThresholdStore, query: PrimaryAvailabilityThresholdQuery): MutableList<PrimaryAvailabilityThreshold> {
    return query.apply(store.list())
}
