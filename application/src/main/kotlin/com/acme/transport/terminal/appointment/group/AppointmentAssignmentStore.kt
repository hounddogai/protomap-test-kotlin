package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment assignments by code. */
interface AppointmentAssignmentStore {
    fun find(code: String): AppointmentAssignment?
    fun save(item: AppointmentAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentAssignmentStore(
    var items: MutableMap<String, AppointmentAssignment>,
    var order: MutableList<String>,
) : AppointmentAssignmentStore {
    override fun find(code: String): AppointmentAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentAssignment) {
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

    override fun list(): MutableList<AppointmentAssignment> {
        val out = mutableListOf<AppointmentAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentAssignmentStore(): AppointmentAssignmentStore {
    return MemoryAppointmentAssignmentStore(
        mutableMapOf<String, AppointmentAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentAssignmentStore(
    var inner: AppointmentAssignmentStore,
    var cache: TtlCache<AppointmentAssignment>,
    var hits: Int,
    var misses: Int,
) : AppointmentAssignmentStore {
    override fun find(code: String): AppointmentAssignment? {
        val hit = (cache.get(code) as AppointmentAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentAssignment> = inner.list()
}

fun newCachedAppointmentAssignmentStore(inner: AppointmentAssignmentStore, ttl: Int, clock: Clock): AppointmentAssignmentStore {
    return CachedAppointmentAssignmentStore(
        inner = inner,
        cache = newTtlCache<AppointmentAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment assignments. Empty fields match all. */
data class AppointmentAssignmentQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentAssignment>): MutableList<AppointmentAssignment> {
        val matched = filter(items) { candidate: AppointmentAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentAssignments(store: AppointmentAssignmentStore, query: AppointmentAssignmentQuery): MutableList<AppointmentAssignment> {
    return query.apply(store.list())
}
