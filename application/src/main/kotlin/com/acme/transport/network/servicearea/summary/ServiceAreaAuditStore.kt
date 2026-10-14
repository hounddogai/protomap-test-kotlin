package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area audits by code. */
interface ServiceAreaAuditStore {
    fun find(code: String): ServiceAreaAudit?
    fun save(item: ServiceAreaAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaAuditStore(
    var items: MutableMap<String, ServiceAreaAudit>,
    var order: MutableList<String>,
) : ServiceAreaAuditStore {
    override fun find(code: String): ServiceAreaAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaAudit) {
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

    override fun list(): MutableList<ServiceAreaAudit> {
        val out = mutableListOf<ServiceAreaAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaAuditStore(): ServiceAreaAuditStore {
    return MemoryServiceAreaAuditStore(mutableMapOf<String, ServiceAreaAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaAuditStore(
    var inner: ServiceAreaAuditStore,
    var cache: TtlCache<ServiceAreaAudit>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaAuditStore {
    override fun find(code: String): ServiceAreaAudit? {
        val hit = (cache.get(code) as ServiceAreaAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaAudit> = inner.list()
}

fun newCachedServiceAreaAuditStore(inner: ServiceAreaAuditStore, ttl: Int, clock: Clock): ServiceAreaAuditStore {
    return CachedServiceAreaAuditStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area audits. Empty fields match all. */
data class ServiceAreaAuditQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServiceAreaAudit>): MutableList<ServiceAreaAudit> {
        val matched = filter(items) { candidate: ServiceAreaAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaAudits(store: ServiceAreaAuditStore, query: ServiceAreaAuditQuery): MutableList<ServiceAreaAudit> {
    return query.apply(store.list())
}
