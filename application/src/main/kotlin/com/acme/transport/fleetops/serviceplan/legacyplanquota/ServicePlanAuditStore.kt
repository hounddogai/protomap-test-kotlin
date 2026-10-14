package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan audits by code. */
interface ServicePlanAuditStore {
    fun find(code: String): ServicePlanAudit?
    fun save(item: ServicePlanAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanAuditStore(
    var items: MutableMap<String, ServicePlanAudit>,
    var order: MutableList<String>,
) : ServicePlanAuditStore {
    override fun find(code: String): ServicePlanAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanAudit) {
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

    override fun list(): MutableList<ServicePlanAudit> {
        val out = mutableListOf<ServicePlanAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanAuditStore(): ServicePlanAuditStore {
    return MemoryServicePlanAuditStore(mutableMapOf<String, ServicePlanAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanAuditStore(
    var inner: ServicePlanAuditStore,
    var cache: TtlCache<ServicePlanAudit>,
    var hits: Int,
    var misses: Int,
) : ServicePlanAuditStore {
    override fun find(code: String): ServicePlanAudit? {
        val hit = (cache.get(code) as ServicePlanAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanAudit> = inner.list()
}

fun newCachedServicePlanAuditStore(inner: ServicePlanAuditStore, ttl: Int, clock: Clock): ServicePlanAuditStore {
    return CachedServicePlanAuditStore(
        inner = inner,
        cache = newTtlCache<ServicePlanAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan audits. Empty fields match all. */
data class ServicePlanAuditQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanAudit): Boolean {
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

    fun apply(items: MutableList<ServicePlanAudit>): MutableList<ServicePlanAudit> {
        val matched = filter(items) { candidate: ServicePlanAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanAudits(store: ServicePlanAuditStore, query: ServicePlanAuditQuery): MutableList<ServicePlanAudit> {
    return query.apply(store.list())
}
