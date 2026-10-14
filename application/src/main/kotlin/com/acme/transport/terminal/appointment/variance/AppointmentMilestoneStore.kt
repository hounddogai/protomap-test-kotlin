package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment milestones by code. */
interface AppointmentMilestoneStore {
    fun find(code: String): AppointmentMilestone?
    fun save(item: AppointmentMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentMilestoneStore(
    var items: MutableMap<String, AppointmentMilestone>,
    var order: MutableList<String>,
) : AppointmentMilestoneStore {
    override fun find(code: String): AppointmentMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentMilestone) {
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

    override fun list(): MutableList<AppointmentMilestone> {
        val out = mutableListOf<AppointmentMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentMilestoneStore(): AppointmentMilestoneStore {
    return MemoryAppointmentMilestoneStore(
        mutableMapOf<String, AppointmentMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentMilestoneStore(
    var inner: AppointmentMilestoneStore,
    var cache: TtlCache<AppointmentMilestone>,
    var hits: Int,
    var misses: Int,
) : AppointmentMilestoneStore {
    override fun find(code: String): AppointmentMilestone? {
        val hit = (cache.get(code) as AppointmentMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentMilestone> = inner.list()
}

fun newCachedAppointmentMilestoneStore(inner: AppointmentMilestoneStore, ttl: Int, clock: Clock): AppointmentMilestoneStore {
    return CachedAppointmentMilestoneStore(
        inner = inner,
        cache = newTtlCache<AppointmentMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment milestones. Empty fields match all. */
data class AppointmentMilestoneQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentMilestone>): MutableList<AppointmentMilestone> {
        val matched = filter(items) { candidate: AppointmentMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentMilestones(store: AppointmentMilestoneStore, query: AppointmentMilestoneQuery): MutableList<AppointmentMilestone> {
    return query.apply(store.list())
}
