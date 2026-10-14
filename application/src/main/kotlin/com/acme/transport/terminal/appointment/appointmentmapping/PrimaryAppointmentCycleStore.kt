package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary appointment cycles by code. */
interface PrimaryAppointmentCycleStore {
    fun find(code: String): PrimaryAppointmentCycle?
    fun save(item: PrimaryAppointmentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAppointmentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAppointmentCycleStore(
    var items: MutableMap<String, PrimaryAppointmentCycle>,
    var order: MutableList<String>,
) : PrimaryAppointmentCycleStore {
    override fun find(code: String): PrimaryAppointmentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAppointmentCycle) {
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

    override fun list(): MutableList<PrimaryAppointmentCycle> {
        val out = mutableListOf<PrimaryAppointmentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAppointmentCycleStore(): PrimaryAppointmentCycleStore {
    return MemoryPrimaryAppointmentCycleStore(
        mutableMapOf<String, PrimaryAppointmentCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAppointmentCycleStore(
    var inner: PrimaryAppointmentCycleStore,
    var cache: TtlCache<PrimaryAppointmentCycle>,
    var hits: Int,
    var misses: Int,
) : PrimaryAppointmentCycleStore {
    override fun find(code: String): PrimaryAppointmentCycle? {
        val hit = (cache.get(code) as PrimaryAppointmentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAppointmentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAppointmentCycle> = inner.list()
}

fun newCachedPrimaryAppointmentCycleStore(inner: PrimaryAppointmentCycleStore, ttl: Int, clock: Clock): PrimaryAppointmentCycleStore {
    return CachedPrimaryAppointmentCycleStore(
        inner = inner,
        cache = newTtlCache<PrimaryAppointmentCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary appointment cycles. Empty fields match all. */
data class PrimaryAppointmentCycleQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAppointmentCycle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAppointmentCycle>): MutableList<PrimaryAppointmentCycle> {
        val matched = filter(items) { candidate: PrimaryAppointmentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAppointmentCycles(store: PrimaryAppointmentCycleStore, query: PrimaryAppointmentCycleQuery): MutableList<PrimaryAppointmentCycle> {
    return query.apply(store.list())
}
