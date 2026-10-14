package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive appointment cycles by code. */
interface AdaptiveAppointmentCycleStore {
    fun find(code: String): AdaptiveAppointmentCycle?
    fun save(item: AdaptiveAppointmentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAppointmentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAppointmentCycleStore(
    var items: MutableMap<String, AdaptiveAppointmentCycle>,
    var order: MutableList<String>,
) : AdaptiveAppointmentCycleStore {
    override fun find(code: String): AdaptiveAppointmentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAppointmentCycle) {
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

    override fun list(): MutableList<AdaptiveAppointmentCycle> {
        val out = mutableListOf<AdaptiveAppointmentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAppointmentCycleStore(): AdaptiveAppointmentCycleStore {
    return MemoryAdaptiveAppointmentCycleStore(
        mutableMapOf<String, AdaptiveAppointmentCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAppointmentCycleStore(
    var inner: AdaptiveAppointmentCycleStore,
    var cache: TtlCache<AdaptiveAppointmentCycle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAppointmentCycleStore {
    override fun find(code: String): AdaptiveAppointmentCycle? {
        val hit = (cache.get(code) as AdaptiveAppointmentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAppointmentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAppointmentCycle> = inner.list()
}

fun newCachedAdaptiveAppointmentCycleStore(inner: AdaptiveAppointmentCycleStore, ttl: Int, clock: Clock): AdaptiveAppointmentCycleStore {
    return CachedAdaptiveAppointmentCycleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAppointmentCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive appointment cycles. Empty fields match all. */
data class AdaptiveAppointmentCycleQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAppointmentCycle): Boolean {
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

    fun apply(items: MutableList<AdaptiveAppointmentCycle>): MutableList<AdaptiveAppointmentCycle> {
        val matched = filter(items) { candidate: AdaptiveAppointmentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAppointmentCycles(store: AdaptiveAppointmentCycleStore, query: AdaptiveAppointmentCycleQuery): MutableList<AdaptiveAppointmentCycle> {
    return query.apply(store.list())
}
