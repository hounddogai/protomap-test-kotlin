package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization audits by code. */
interface UtilizationAuditStore {
    fun find(code: String): UtilizationAudit?
    fun save(item: UtilizationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationAuditStore(
    var items: MutableMap<String, UtilizationAudit>,
    var order: MutableList<String>,
) : UtilizationAuditStore {
    override fun find(code: String): UtilizationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationAudit) {
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

    override fun list(): MutableList<UtilizationAudit> {
        val out = mutableListOf<UtilizationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationAuditStore(): UtilizationAuditStore {
    return MemoryUtilizationAuditStore(mutableMapOf<String, UtilizationAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationAuditStore(
    var inner: UtilizationAuditStore,
    var cache: TtlCache<UtilizationAudit>,
    var hits: Int,
    var misses: Int,
) : UtilizationAuditStore {
    override fun find(code: String): UtilizationAudit? {
        val hit = (cache.get(code) as UtilizationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationAudit> = inner.list()
}

fun newCachedUtilizationAuditStore(inner: UtilizationAuditStore, ttl: Int, clock: Clock): UtilizationAuditStore {
    return CachedUtilizationAuditStore(
        inner = inner,
        cache = newTtlCache<UtilizationAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization audits. Empty fields match all. */
data class UtilizationAuditQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<UtilizationAudit>): MutableList<UtilizationAudit> {
        val matched = filter(items) { candidate: UtilizationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationAudits(store: UtilizationAuditStore, query: UtilizationAuditQuery): MutableList<UtilizationAudit> {
    return query.apply(store.list())
}
