package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment audits by code. */
interface AppointmentAuditStore {
    fun find(code: String): AppointmentAudit?
    fun save(item: AppointmentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentAuditStore(
    var items: MutableMap<String, AppointmentAudit>,
    var order: MutableList<String>,
) : AppointmentAuditStore {
    override fun find(code: String): AppointmentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentAudit) {
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

    override fun list(): MutableList<AppointmentAudit> {
        val out = mutableListOf<AppointmentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentAuditStore(): AppointmentAuditStore {
    return MemoryAppointmentAuditStore(mutableMapOf<String, AppointmentAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentAuditStore(
    var inner: AppointmentAuditStore,
    var cache: TtlCache<AppointmentAudit>,
    var hits: Int,
    var misses: Int,
) : AppointmentAuditStore {
    override fun find(code: String): AppointmentAudit? {
        val hit = (cache.get(code) as AppointmentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentAudit> = inner.list()
}

fun newCachedAppointmentAuditStore(inner: AppointmentAuditStore, ttl: Int, clock: Clock): AppointmentAuditStore {
    return CachedAppointmentAuditStore(
        inner = inner,
        cache = newTtlCache<AppointmentAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment audits. Empty fields match all. */
data class AppointmentAuditQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentAudit>): MutableList<AppointmentAudit> {
        val matched = filter(items) { candidate: AppointmentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentAudits(store: AppointmentAuditStore, query: AppointmentAuditQuery): MutableList<AppointmentAudit> {
    return query.apply(store.list())
}
