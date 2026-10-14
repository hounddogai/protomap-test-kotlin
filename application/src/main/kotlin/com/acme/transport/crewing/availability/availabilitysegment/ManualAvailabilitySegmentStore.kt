package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual availability segments by code. */
interface ManualAvailabilitySegmentStore {
    fun find(code: String): ManualAvailabilitySegment?
    fun save(item: ManualAvailabilitySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAvailabilitySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAvailabilitySegmentStore(
    var items: MutableMap<String, ManualAvailabilitySegment>,
    var order: MutableList<String>,
) : ManualAvailabilitySegmentStore {
    override fun find(code: String): ManualAvailabilitySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAvailabilitySegment) {
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

    override fun list(): MutableList<ManualAvailabilitySegment> {
        val out = mutableListOf<ManualAvailabilitySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAvailabilitySegmentStore(): ManualAvailabilitySegmentStore {
    return MemoryManualAvailabilitySegmentStore(
        mutableMapOf<String, ManualAvailabilitySegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAvailabilitySegmentStore(
    var inner: ManualAvailabilitySegmentStore,
    var cache: TtlCache<ManualAvailabilitySegment>,
    var hits: Int,
    var misses: Int,
) : ManualAvailabilitySegmentStore {
    override fun find(code: String): ManualAvailabilitySegment? {
        val hit = (cache.get(code) as ManualAvailabilitySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAvailabilitySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAvailabilitySegment> = inner.list()
}

fun newCachedManualAvailabilitySegmentStore(inner: ManualAvailabilitySegmentStore, ttl: Int, clock: Clock): ManualAvailabilitySegmentStore {
    return CachedManualAvailabilitySegmentStore(
        inner = inner,
        cache = newTtlCache<ManualAvailabilitySegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual availability segments. Empty fields match all. */
data class ManualAvailabilitySegmentQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAvailabilitySegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAvailabilitySegment>): MutableList<ManualAvailabilitySegment> {
        val matched = filter(items) { candidate: ManualAvailabilitySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAvailabilitySegments(store: ManualAvailabilitySegmentStore, query: ManualAvailabilitySegmentQuery): MutableList<ManualAvailabilitySegment> {
    return query.apply(store.list())
}
