package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment schedules by code. */
interface AppointmentScheduleStore {
    fun find(code: String): AppointmentSchedule?
    fun save(item: AppointmentSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentScheduleStore(
    var items: MutableMap<String, AppointmentSchedule>,
    var order: MutableList<String>,
) : AppointmentScheduleStore {
    override fun find(code: String): AppointmentSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentSchedule) {
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

    override fun list(): MutableList<AppointmentSchedule> {
        val out = mutableListOf<AppointmentSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentScheduleStore(): AppointmentScheduleStore {
    return MemoryAppointmentScheduleStore(mutableMapOf<String, AppointmentSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentScheduleStore(
    var inner: AppointmentScheduleStore,
    var cache: TtlCache<AppointmentSchedule>,
    var hits: Int,
    var misses: Int,
) : AppointmentScheduleStore {
    override fun find(code: String): AppointmentSchedule? {
        val hit = (cache.get(code) as AppointmentSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentSchedule> = inner.list()
}

fun newCachedAppointmentScheduleStore(inner: AppointmentScheduleStore, ttl: Int, clock: Clock): AppointmentScheduleStore {
    return CachedAppointmentScheduleStore(
        inner = inner,
        cache = newTtlCache<AppointmentSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment schedules. Empty fields match all. */
data class AppointmentScheduleQuery(
    var segment: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentSchedule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentSchedule>): MutableList<AppointmentSchedule> {
        val matched = filter(items) { candidate: AppointmentSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentSchedules(store: AppointmentScheduleStore, query: AppointmentScheduleQuery): MutableList<AppointmentSchedule> {
    return query.apply(store.list())
}
