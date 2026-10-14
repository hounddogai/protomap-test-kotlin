package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset audits by code. */
interface RouteOffsetAuditStore {
    fun find(code: String): RouteOffsetAudit?
    fun save(item: RouteOffsetAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetAuditStore(
    var items: MutableMap<String, RouteOffsetAudit>,
    var order: MutableList<String>,
) : RouteOffsetAuditStore {
    override fun find(code: String): RouteOffsetAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetAudit) {
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

    override fun list(): MutableList<RouteOffsetAudit> {
        val out = mutableListOf<RouteOffsetAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetAuditStore(): RouteOffsetAuditStore {
    return MemoryRouteOffsetAuditStore(mutableMapOf<String, RouteOffsetAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetAuditStore(
    var inner: RouteOffsetAuditStore,
    var cache: TtlCache<RouteOffsetAudit>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetAuditStore {
    override fun find(code: String): RouteOffsetAudit? {
        val hit = (cache.get(code) as RouteOffsetAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetAudit> = inner.list()
}

fun newCachedRouteOffsetAuditStore(inner: RouteOffsetAuditStore, ttl: Int, clock: Clock): RouteOffsetAuditStore {
    return CachedRouteOffsetAuditStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset audits. Empty fields match all. */
data class RouteOffsetAuditQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetAudit>): MutableList<RouteOffsetAudit> {
        val matched = filter(items) { candidate: RouteOffsetAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetAudits(store: RouteOffsetAuditStore, query: RouteOffsetAuditQuery): MutableList<RouteOffsetAudit> {
    return query.apply(store.list())
}
