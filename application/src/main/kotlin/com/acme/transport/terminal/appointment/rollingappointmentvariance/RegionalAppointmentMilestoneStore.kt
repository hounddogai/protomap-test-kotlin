package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional appointment milestones by code. */
interface RegionalAppointmentMilestoneStore {
    fun find(code: String): RegionalAppointmentMilestone?
    fun save(item: RegionalAppointmentMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAppointmentMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAppointmentMilestoneStore(
    var items: MutableMap<String, RegionalAppointmentMilestone>,
    var order: MutableList<String>,
) : RegionalAppointmentMilestoneStore {
    override fun find(code: String): RegionalAppointmentMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAppointmentMilestone) {
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

    override fun list(): MutableList<RegionalAppointmentMilestone> {
        val out = mutableListOf<RegionalAppointmentMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAppointmentMilestoneStore(): RegionalAppointmentMilestoneStore {
    return MemoryRegionalAppointmentMilestoneStore(
        mutableMapOf<String, RegionalAppointmentMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAppointmentMilestoneStore(
    var inner: RegionalAppointmentMilestoneStore,
    var cache: TtlCache<RegionalAppointmentMilestone>,
    var hits: Int,
    var misses: Int,
) : RegionalAppointmentMilestoneStore {
    override fun find(code: String): RegionalAppointmentMilestone? {
        val hit = (cache.get(code) as RegionalAppointmentMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAppointmentMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAppointmentMilestone> = inner.list()
}

fun newCachedRegionalAppointmentMilestoneStore(inner: RegionalAppointmentMilestoneStore, ttl: Int, clock: Clock): RegionalAppointmentMilestoneStore {
    return CachedRegionalAppointmentMilestoneStore(
        inner = inner,
        cache = newTtlCache<RegionalAppointmentMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional appointment milestones. Empty fields match all. */
data class RegionalAppointmentMilestoneQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAppointmentMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAppointmentMilestone>): MutableList<RegionalAppointmentMilestone> {
        val matched = filter(items) { candidate: RegionalAppointmentMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAppointmentMilestones(store: RegionalAppointmentMilestoneStore, query: RegionalAppointmentMilestoneQuery): MutableList<RegionalAppointmentMilestone> {
    return query.apply(store.list())
}
