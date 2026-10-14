package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy appointment segments by code. */
interface LegacyAppointmentSegmentStore {
    fun find(code: String): LegacyAppointmentSegment?
    fun save(item: LegacyAppointmentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAppointmentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAppointmentSegmentStore(
    var items: MutableMap<String, LegacyAppointmentSegment>,
    var order: MutableList<String>,
) : LegacyAppointmentSegmentStore {
    override fun find(code: String): LegacyAppointmentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAppointmentSegment) {
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

    override fun list(): MutableList<LegacyAppointmentSegment> {
        val out = mutableListOf<LegacyAppointmentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAppointmentSegmentStore(): LegacyAppointmentSegmentStore {
    return MemoryLegacyAppointmentSegmentStore(
        mutableMapOf<String, LegacyAppointmentSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAppointmentSegmentStore(
    var inner: LegacyAppointmentSegmentStore,
    var cache: TtlCache<LegacyAppointmentSegment>,
    var hits: Int,
    var misses: Int,
) : LegacyAppointmentSegmentStore {
    override fun find(code: String): LegacyAppointmentSegment? {
        val hit = (cache.get(code) as LegacyAppointmentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAppointmentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAppointmentSegment> = inner.list()
}

fun newCachedLegacyAppointmentSegmentStore(inner: LegacyAppointmentSegmentStore, ttl: Int, clock: Clock): LegacyAppointmentSegmentStore {
    return CachedLegacyAppointmentSegmentStore(
        inner = inner,
        cache = newTtlCache<LegacyAppointmentSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy appointment segments. Empty fields match all. */
data class LegacyAppointmentSegmentQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAppointmentSegment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAppointmentSegment>): MutableList<LegacyAppointmentSegment> {
        val matched = filter(items) { candidate: LegacyAppointmentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAppointmentSegments(store: LegacyAppointmentSegmentStore, query: LegacyAppointmentSegmentQuery): MutableList<LegacyAppointmentSegment> {
    return query.apply(store.list())
}
