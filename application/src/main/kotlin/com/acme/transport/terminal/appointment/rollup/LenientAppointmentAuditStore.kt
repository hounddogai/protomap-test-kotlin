package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient appointment audits by code. */
interface LenientAppointmentAuditStore {
    fun find(code: String): LenientAppointmentAudit?
    fun save(item: LenientAppointmentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAppointmentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAppointmentAuditStore(
    var items: MutableMap<String, LenientAppointmentAudit>,
    var order: MutableList<String>,
) : LenientAppointmentAuditStore {
    override fun find(code: String): LenientAppointmentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAppointmentAudit) {
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

    override fun list(): MutableList<LenientAppointmentAudit> {
        val out = mutableListOf<LenientAppointmentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAppointmentAuditStore(): LenientAppointmentAuditStore {
    return MemoryLenientAppointmentAuditStore(
        mutableMapOf<String, LenientAppointmentAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAppointmentAuditStore(
    var inner: LenientAppointmentAuditStore,
    var cache: TtlCache<LenientAppointmentAudit>,
    var hits: Int,
    var misses: Int,
) : LenientAppointmentAuditStore {
    override fun find(code: String): LenientAppointmentAudit? {
        val hit = (cache.get(code) as LenientAppointmentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAppointmentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAppointmentAudit> = inner.list()
}

fun newCachedLenientAppointmentAuditStore(inner: LenientAppointmentAuditStore, ttl: Int, clock: Clock): LenientAppointmentAuditStore {
    return CachedLenientAppointmentAuditStore(
        inner = inner,
        cache = newTtlCache<LenientAppointmentAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient appointment audits. Empty fields match all. */
data class LenientAppointmentAuditQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAppointmentAudit): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAppointmentAudit>): MutableList<LenientAppointmentAudit> {
        val matched = filter(items) { candidate: LenientAppointmentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAppointmentAudits(store: LenientAppointmentAuditStore, query: LenientAppointmentAuditQuery): MutableList<LenientAppointmentAudit> {
    return query.apply(store.list())
}
