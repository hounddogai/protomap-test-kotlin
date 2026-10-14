package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal appointment audits by code. */
interface SeasonalAppointmentAuditStore {
    fun find(code: String): SeasonalAppointmentAudit?
    fun save(item: SeasonalAppointmentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAppointmentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAppointmentAuditStore(
    var items: MutableMap<String, SeasonalAppointmentAudit>,
    var order: MutableList<String>,
) : SeasonalAppointmentAuditStore {
    override fun find(code: String): SeasonalAppointmentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAppointmentAudit) {
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

    override fun list(): MutableList<SeasonalAppointmentAudit> {
        val out = mutableListOf<SeasonalAppointmentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAppointmentAuditStore(): SeasonalAppointmentAuditStore {
    return MemorySeasonalAppointmentAuditStore(
        mutableMapOf<String, SeasonalAppointmentAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAppointmentAuditStore(
    var inner: SeasonalAppointmentAuditStore,
    var cache: TtlCache<SeasonalAppointmentAudit>,
    var hits: Int,
    var misses: Int,
) : SeasonalAppointmentAuditStore {
    override fun find(code: String): SeasonalAppointmentAudit? {
        val hit = (cache.get(code) as SeasonalAppointmentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAppointmentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAppointmentAudit> = inner.list()
}

fun newCachedSeasonalAppointmentAuditStore(inner: SeasonalAppointmentAuditStore, ttl: Int, clock: Clock): SeasonalAppointmentAuditStore {
    return CachedSeasonalAppointmentAuditStore(
        inner = inner,
        cache = newTtlCache<SeasonalAppointmentAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal appointment audits. Empty fields match all. */
data class SeasonalAppointmentAuditQuery(
    var segment: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAppointmentAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<SeasonalAppointmentAudit>): MutableList<SeasonalAppointmentAudit> {
        val matched = filter(items) { candidate: SeasonalAppointmentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAppointmentAudits(store: SeasonalAppointmentAuditStore, query: SeasonalAppointmentAuditQuery): MutableList<SeasonalAppointmentAudit> {
    return query.apply(store.list())
}
