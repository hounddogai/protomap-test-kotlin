package com.acme.transport.fleetops.defect.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled defect audits by code. */
interface ScheduledDefectAuditStore {
    fun find(code: String): ScheduledDefectAudit?
    fun save(item: ScheduledDefectAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDefectAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDefectAuditStore(
    var items: MutableMap<String, ScheduledDefectAudit>,
    var order: MutableList<String>,
) : ScheduledDefectAuditStore {
    override fun find(code: String): ScheduledDefectAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDefectAudit) {
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

    override fun list(): MutableList<ScheduledDefectAudit> {
        val out = mutableListOf<ScheduledDefectAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDefectAuditStore(): ScheduledDefectAuditStore {
    return MemoryScheduledDefectAuditStore(
        mutableMapOf<String, ScheduledDefectAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDefectAuditStore(
    var inner: ScheduledDefectAuditStore,
    var cache: TtlCache<ScheduledDefectAudit>,
    var hits: Int,
    var misses: Int,
) : ScheduledDefectAuditStore {
    override fun find(code: String): ScheduledDefectAudit? {
        val hit = (cache.get(code) as ScheduledDefectAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDefectAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDefectAudit> = inner.list()
}

fun newCachedScheduledDefectAuditStore(inner: ScheduledDefectAuditStore, ttl: Int, clock: Clock): ScheduledDefectAuditStore {
    return CachedScheduledDefectAuditStore(
        inner = inner,
        cache = newTtlCache<ScheduledDefectAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled defect audits. Empty fields match all. */
data class ScheduledDefectAuditQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDefectAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDefectAudit>): MutableList<ScheduledDefectAudit> {
        val matched = filter(items) { candidate: ScheduledDefectAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDefectAudits(store: ScheduledDefectAuditStore, query: ScheduledDefectAuditQuery): MutableList<ScheduledDefectAudit> {
    return query.apply(store.list())
}
