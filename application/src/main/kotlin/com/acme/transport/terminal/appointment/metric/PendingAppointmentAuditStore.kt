package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending appointment audits by code. */
interface PendingAppointmentAuditStore {
    fun find(code: String): PendingAppointmentAudit?
    fun save(item: PendingAppointmentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAppointmentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAppointmentAuditStore(
    var items: MutableMap<String, PendingAppointmentAudit>,
    var order: MutableList<String>,
) : PendingAppointmentAuditStore {
    override fun find(code: String): PendingAppointmentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAppointmentAudit) {
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

    override fun list(): MutableList<PendingAppointmentAudit> {
        val out = mutableListOf<PendingAppointmentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAppointmentAuditStore(): PendingAppointmentAuditStore {
    return MemoryPendingAppointmentAuditStore(
        mutableMapOf<String, PendingAppointmentAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAppointmentAuditStore(
    var inner: PendingAppointmentAuditStore,
    var cache: TtlCache<PendingAppointmentAudit>,
    var hits: Int,
    var misses: Int,
) : PendingAppointmentAuditStore {
    override fun find(code: String): PendingAppointmentAudit? {
        val hit = (cache.get(code) as PendingAppointmentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAppointmentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAppointmentAudit> = inner.list()
}

fun newCachedPendingAppointmentAuditStore(inner: PendingAppointmentAuditStore, ttl: Int, clock: Clock): PendingAppointmentAuditStore {
    return CachedPendingAppointmentAuditStore(
        inner = inner,
        cache = newTtlCache<PendingAppointmentAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending appointment audits. Empty fields match all. */
data class PendingAppointmentAuditQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAppointmentAudit): Boolean {
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

    fun apply(items: MutableList<PendingAppointmentAudit>): MutableList<PendingAppointmentAudit> {
        val matched = filter(items) { candidate: PendingAppointmentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAppointmentAudits(store: PendingAppointmentAuditStore, query: PendingAppointmentAuditQuery): MutableList<PendingAppointmentAudit> {
    return query.apply(store.list())
}
