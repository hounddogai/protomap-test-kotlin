package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster audits by code. */
interface RosterAuditStore {
    fun find(code: String): RosterAudit?
    fun save(item: RosterAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterAuditStore(
    var items: MutableMap<String, RosterAudit>,
    var order: MutableList<String>,
) : RosterAuditStore {
    override fun find(code: String): RosterAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterAudit) {
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

    override fun list(): MutableList<RosterAudit> {
        val out = mutableListOf<RosterAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterAuditStore(): RosterAuditStore {
    return MemoryRosterAuditStore(mutableMapOf<String, RosterAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterAuditStore(
    var inner: RosterAuditStore,
    var cache: TtlCache<RosterAudit>,
    var hits: Int,
    var misses: Int,
) : RosterAuditStore {
    override fun find(code: String): RosterAudit? {
        val hit = (cache.get(code) as RosterAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterAudit> = inner.list()
}

fun newCachedRosterAuditStore(inner: RosterAuditStore, ttl: Int, clock: Clock): RosterAuditStore {
    return CachedRosterAuditStore(
        inner = inner,
        cache = newTtlCache<RosterAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster audits. Empty fields match all. */
data class RosterAuditQuery(
    var segment: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RosterAudit>): MutableList<RosterAudit> {
        val matched = filter(items) { candidate: RosterAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterAudits(store: RosterAuditStore, query: RosterAuditQuery): MutableList<RosterAudit> {
    return query.apply(store.list())
}
