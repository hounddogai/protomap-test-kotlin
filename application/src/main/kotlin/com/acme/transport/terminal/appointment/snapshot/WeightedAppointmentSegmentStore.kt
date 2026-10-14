package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted appointment segments by code. */
interface WeightedAppointmentSegmentStore {
    fun find(code: String): WeightedAppointmentSegment?
    fun save(item: WeightedAppointmentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAppointmentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAppointmentSegmentStore(
    var items: MutableMap<String, WeightedAppointmentSegment>,
    var order: MutableList<String>,
) : WeightedAppointmentSegmentStore {
    override fun find(code: String): WeightedAppointmentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAppointmentSegment) {
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

    override fun list(): MutableList<WeightedAppointmentSegment> {
        val out = mutableListOf<WeightedAppointmentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAppointmentSegmentStore(): WeightedAppointmentSegmentStore {
    return MemoryWeightedAppointmentSegmentStore(
        mutableMapOf<String, WeightedAppointmentSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAppointmentSegmentStore(
    var inner: WeightedAppointmentSegmentStore,
    var cache: TtlCache<WeightedAppointmentSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedAppointmentSegmentStore {
    override fun find(code: String): WeightedAppointmentSegment? {
        val hit = (cache.get(code) as WeightedAppointmentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAppointmentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAppointmentSegment> = inner.list()
}

fun newCachedWeightedAppointmentSegmentStore(inner: WeightedAppointmentSegmentStore, ttl: Int, clock: Clock): WeightedAppointmentSegmentStore {
    return CachedWeightedAppointmentSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedAppointmentSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted appointment segments. Empty fields match all. */
data class WeightedAppointmentSegmentQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAppointmentSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAppointmentSegment>): MutableList<WeightedAppointmentSegment> {
        val matched = filter(items) { candidate: WeightedAppointmentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAppointmentSegments(store: WeightedAppointmentSegmentStore, query: WeightedAppointmentSegmentQuery): MutableList<WeightedAppointmentSegment> {
    return query.apply(store.list())
}
