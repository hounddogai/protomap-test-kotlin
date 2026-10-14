package com.acme.transport.crewing.availability.defaultavailabilitysegment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled availability segments by code. */
interface ScheduledAvailabilitySegmentStore {
    fun find(code: String): ScheduledAvailabilitySegment?
    fun save(item: ScheduledAvailabilitySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAvailabilitySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAvailabilitySegmentStore(
    var items: MutableMap<String, ScheduledAvailabilitySegment>,
    var order: MutableList<String>,
) : ScheduledAvailabilitySegmentStore {
    override fun find(code: String): ScheduledAvailabilitySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAvailabilitySegment) {
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

    override fun list(): MutableList<ScheduledAvailabilitySegment> {
        val out = mutableListOf<ScheduledAvailabilitySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAvailabilitySegmentStore(): ScheduledAvailabilitySegmentStore {
    return MemoryScheduledAvailabilitySegmentStore(
        mutableMapOf<String, ScheduledAvailabilitySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAvailabilitySegmentStore(
    var inner: ScheduledAvailabilitySegmentStore,
    var cache: TtlCache<ScheduledAvailabilitySegment>,
    var hits: Int,
    var misses: Int,
) : ScheduledAvailabilitySegmentStore {
    override fun find(code: String): ScheduledAvailabilitySegment? {
        val hit = (cache.get(code) as ScheduledAvailabilitySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAvailabilitySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAvailabilitySegment> = inner.list()
}

fun newCachedScheduledAvailabilitySegmentStore(inner: ScheduledAvailabilitySegmentStore, ttl: Int, clock: Clock): ScheduledAvailabilitySegmentStore {
    return CachedScheduledAvailabilitySegmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledAvailabilitySegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled availability segments. Empty fields match all. */
data class ScheduledAvailabilitySegmentQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAvailabilitySegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledAvailabilitySegment>): MutableList<ScheduledAvailabilitySegment> {
        val matched = filter(items) { candidate: ScheduledAvailabilitySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAvailabilitySegments(store: ScheduledAvailabilitySegmentStore, query: ScheduledAvailabilitySegmentQuery): MutableList<ScheduledAvailabilitySegment> {
    return query.apply(store.list())
}
