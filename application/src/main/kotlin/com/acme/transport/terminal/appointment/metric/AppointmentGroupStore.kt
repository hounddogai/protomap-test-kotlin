package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment groups by code. */
interface AppointmentGroupStore {
    fun find(code: String): AppointmentGroup?
    fun save(item: AppointmentGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentGroupStore(
    var items: MutableMap<String, AppointmentGroup>,
    var order: MutableList<String>,
) : AppointmentGroupStore {
    override fun find(code: String): AppointmentGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentGroup) {
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

    override fun list(): MutableList<AppointmentGroup> {
        val out = mutableListOf<AppointmentGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentGroupStore(): AppointmentGroupStore {
    return MemoryAppointmentGroupStore(mutableMapOf<String, AppointmentGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentGroupStore(
    var inner: AppointmentGroupStore,
    var cache: TtlCache<AppointmentGroup>,
    var hits: Int,
    var misses: Int,
) : AppointmentGroupStore {
    override fun find(code: String): AppointmentGroup? {
        val hit = (cache.get(code) as AppointmentGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentGroup> = inner.list()
}

fun newCachedAppointmentGroupStore(inner: AppointmentGroupStore, ttl: Int, clock: Clock): AppointmentGroupStore {
    return CachedAppointmentGroupStore(
        inner = inner,
        cache = newTtlCache<AppointmentGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment groups. Empty fields match all. */
data class AppointmentGroupQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentGroup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentGroup>): MutableList<AppointmentGroup> {
        val matched = filter(items) { candidate: AppointmentGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentGroups(store: AppointmentGroupStore, query: AppointmentGroupQuery): MutableList<AppointmentGroup> {
    return query.apply(store.list())
}
