package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment segments by code. */
interface AppointmentSegmentStore {
    fun find(code: String): AppointmentSegment?
    fun save(item: AppointmentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentSegmentStore(
    var items: MutableMap<String, AppointmentSegment>,
    var order: MutableList<String>,
) : AppointmentSegmentStore {
    override fun find(code: String): AppointmentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentSegment) {
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

    override fun list(): MutableList<AppointmentSegment> {
        val out = mutableListOf<AppointmentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentSegmentStore(): AppointmentSegmentStore {
    return MemoryAppointmentSegmentStore(mutableMapOf<String, AppointmentSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentSegmentStore(
    var inner: AppointmentSegmentStore,
    var cache: TtlCache<AppointmentSegment>,
    var hits: Int,
    var misses: Int,
) : AppointmentSegmentStore {
    override fun find(code: String): AppointmentSegment? {
        val hit = (cache.get(code) as AppointmentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentSegment> = inner.list()
}

fun newCachedAppointmentSegmentStore(inner: AppointmentSegmentStore, ttl: Int, clock: Clock): AppointmentSegmentStore {
    return CachedAppointmentSegmentStore(
        inner = inner,
        cache = newTtlCache<AppointmentSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment segments. Empty fields match all. */
data class AppointmentSegmentQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentSegment>): MutableList<AppointmentSegment> {
        val matched = filter(items) { candidate: AppointmentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentSegments(store: AppointmentSegmentStore, query: AppointmentSegmentQuery): MutableList<AppointmentSegment> {
    return query.apply(store.list())
}
