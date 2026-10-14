package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment cycles by code. */
interface AppointmentCycleStore {
    fun find(code: String): AppointmentCycle?
    fun save(item: AppointmentCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentCycleStore(
    var items: MutableMap<String, AppointmentCycle>,
    var order: MutableList<String>,
) : AppointmentCycleStore {
    override fun find(code: String): AppointmentCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentCycle) {
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

    override fun list(): MutableList<AppointmentCycle> {
        val out = mutableListOf<AppointmentCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentCycleStore(): AppointmentCycleStore {
    return MemoryAppointmentCycleStore(mutableMapOf<String, AppointmentCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentCycleStore(
    var inner: AppointmentCycleStore,
    var cache: TtlCache<AppointmentCycle>,
    var hits: Int,
    var misses: Int,
) : AppointmentCycleStore {
    override fun find(code: String): AppointmentCycle? {
        val hit = (cache.get(code) as AppointmentCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentCycle> = inner.list()
}

fun newCachedAppointmentCycleStore(inner: AppointmentCycleStore, ttl: Int, clock: Clock): AppointmentCycleStore {
    return CachedAppointmentCycleStore(
        inner = inner,
        cache = newTtlCache<AppointmentCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment cycles. Empty fields match all. */
data class AppointmentCycleQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentCycle>): MutableList<AppointmentCycle> {
        val matched = filter(items) { candidate: AppointmentCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentCycles(store: AppointmentCycleStore, query: AppointmentCycleQuery): MutableList<AppointmentCycle> {
    return query.apply(store.list())
}
