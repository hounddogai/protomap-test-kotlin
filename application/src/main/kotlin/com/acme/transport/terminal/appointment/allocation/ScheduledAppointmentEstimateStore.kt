package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled appointment estimates by code. */
interface ScheduledAppointmentEstimateStore {
    fun find(code: String): ScheduledAppointmentEstimate?
    fun save(item: ScheduledAppointmentEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAppointmentEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAppointmentEstimateStore(
    var items: MutableMap<String, ScheduledAppointmentEstimate>,
    var order: MutableList<String>,
) : ScheduledAppointmentEstimateStore {
    override fun find(code: String): ScheduledAppointmentEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAppointmentEstimate) {
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

    override fun list(): MutableList<ScheduledAppointmentEstimate> {
        val out = mutableListOf<ScheduledAppointmentEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAppointmentEstimateStore(): ScheduledAppointmentEstimateStore {
    return MemoryScheduledAppointmentEstimateStore(
        mutableMapOf<String, ScheduledAppointmentEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAppointmentEstimateStore(
    var inner: ScheduledAppointmentEstimateStore,
    var cache: TtlCache<ScheduledAppointmentEstimate>,
    var hits: Int,
    var misses: Int,
) : ScheduledAppointmentEstimateStore {
    override fun find(code: String): ScheduledAppointmentEstimate? {
        val hit = (cache.get(code) as ScheduledAppointmentEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAppointmentEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAppointmentEstimate> = inner.list()
}

fun newCachedScheduledAppointmentEstimateStore(inner: ScheduledAppointmentEstimateStore, ttl: Int, clock: Clock): ScheduledAppointmentEstimateStore {
    return CachedScheduledAppointmentEstimateStore(
        inner = inner,
        cache = newTtlCache<ScheduledAppointmentEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled appointment estimates. Empty fields match all. */
data class ScheduledAppointmentEstimateQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAppointmentEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ScheduledAppointmentEstimate>): MutableList<ScheduledAppointmentEstimate> {
        val matched = filter(items) { candidate: ScheduledAppointmentEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAppointmentEstimates(store: ScheduledAppointmentEstimateStore, query: ScheduledAppointmentEstimateQuery): MutableList<ScheduledAppointmentEstimate> {
    return query.apply(store.list())
}
