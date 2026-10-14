package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn segments by code. */
interface FuelBurnSegmentStore {
    fun find(code: String): FuelBurnSegment?
    fun save(item: FuelBurnSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnSegmentStore(
    var items: MutableMap<String, FuelBurnSegment>,
    var order: MutableList<String>,
) : FuelBurnSegmentStore {
    override fun find(code: String): FuelBurnSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnSegment) {
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

    override fun list(): MutableList<FuelBurnSegment> {
        val out = mutableListOf<FuelBurnSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnSegmentStore(): FuelBurnSegmentStore {
    return MemoryFuelBurnSegmentStore(mutableMapOf<String, FuelBurnSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnSegmentStore(
    var inner: FuelBurnSegmentStore,
    var cache: TtlCache<FuelBurnSegment>,
    var hits: Int,
    var misses: Int,
) : FuelBurnSegmentStore {
    override fun find(code: String): FuelBurnSegment? {
        val hit = (cache.get(code) as FuelBurnSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnSegment> = inner.list()
}

fun newCachedFuelBurnSegmentStore(inner: FuelBurnSegmentStore, ttl: Int, clock: Clock): FuelBurnSegmentStore {
    return CachedFuelBurnSegmentStore(
        inner = inner,
        cache = newTtlCache<FuelBurnSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn segments. Empty fields match all. */
data class FuelBurnSegmentQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnSegment>): MutableList<FuelBurnSegment> {
        val matched = filter(items) { candidate: FuelBurnSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnSegments(store: FuelBurnSegmentStore, query: FuelBurnSegmentQuery): MutableList<FuelBurnSegment> {
    return query.apply(store.list())
}
