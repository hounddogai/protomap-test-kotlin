package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted utilization audits by code. */
interface WeightedUtilizationAuditStore {
    fun find(code: String): WeightedUtilizationAudit?
    fun save(item: WeightedUtilizationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedUtilizationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedUtilizationAuditStore(
    var items: MutableMap<String, WeightedUtilizationAudit>,
    var order: MutableList<String>,
) : WeightedUtilizationAuditStore {
    override fun find(code: String): WeightedUtilizationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedUtilizationAudit) {
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

    override fun list(): MutableList<WeightedUtilizationAudit> {
        val out = mutableListOf<WeightedUtilizationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedUtilizationAuditStore(): WeightedUtilizationAuditStore {
    return MemoryWeightedUtilizationAuditStore(
        mutableMapOf<String, WeightedUtilizationAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedUtilizationAuditStore(
    var inner: WeightedUtilizationAuditStore,
    var cache: TtlCache<WeightedUtilizationAudit>,
    var hits: Int,
    var misses: Int,
) : WeightedUtilizationAuditStore {
    override fun find(code: String): WeightedUtilizationAudit? {
        val hit = (cache.get(code) as WeightedUtilizationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedUtilizationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedUtilizationAudit> = inner.list()
}

fun newCachedWeightedUtilizationAuditStore(inner: WeightedUtilizationAuditStore, ttl: Int, clock: Clock): WeightedUtilizationAuditStore {
    return CachedWeightedUtilizationAuditStore(
        inner = inner,
        cache = newTtlCache<WeightedUtilizationAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted utilization audits. Empty fields match all. */
data class WeightedUtilizationAuditQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedUtilizationAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedUtilizationAudit>): MutableList<WeightedUtilizationAudit> {
        val matched = filter(items) { candidate: WeightedUtilizationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedUtilizationAudits(store: WeightedUtilizationAuditStore, query: WeightedUtilizationAuditQuery): MutableList<WeightedUtilizationAudit> {
    return query.apply(store.list())
}
