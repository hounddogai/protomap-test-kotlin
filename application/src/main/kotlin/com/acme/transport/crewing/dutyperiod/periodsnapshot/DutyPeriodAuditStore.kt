package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period audits by code. */
interface DutyPeriodAuditStore {
    fun find(code: String): DutyPeriodAudit?
    fun save(item: DutyPeriodAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodAuditStore(
    var items: MutableMap<String, DutyPeriodAudit>,
    var order: MutableList<String>,
) : DutyPeriodAuditStore {
    override fun find(code: String): DutyPeriodAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodAudit) {
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

    override fun list(): MutableList<DutyPeriodAudit> {
        val out = mutableListOf<DutyPeriodAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodAuditStore(): DutyPeriodAuditStore {
    return MemoryDutyPeriodAuditStore(mutableMapOf<String, DutyPeriodAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodAuditStore(
    var inner: DutyPeriodAuditStore,
    var cache: TtlCache<DutyPeriodAudit>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodAuditStore {
    override fun find(code: String): DutyPeriodAudit? {
        val hit = (cache.get(code) as DutyPeriodAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodAudit> = inner.list()
}

fun newCachedDutyPeriodAuditStore(inner: DutyPeriodAuditStore, ttl: Int, clock: Clock): DutyPeriodAuditStore {
    return CachedDutyPeriodAuditStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period audits. Empty fields match all. */
data class DutyPeriodAuditQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DutyPeriodAudit>): MutableList<DutyPeriodAudit> {
        val matched = filter(items) { candidate: DutyPeriodAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodAudits(store: DutyPeriodAuditStore, query: DutyPeriodAuditQuery): MutableList<DutyPeriodAudit> {
    return query.apply(store.list())
}
