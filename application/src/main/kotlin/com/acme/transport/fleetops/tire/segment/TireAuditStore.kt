package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire audits by code. */
interface TireAuditStore {
    fun find(code: String): TireAudit?
    fun save(item: TireAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireAuditStore(
    var items: MutableMap<String, TireAudit>,
    var order: MutableList<String>,
) : TireAuditStore {
    override fun find(code: String): TireAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireAudit) {
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

    override fun list(): MutableList<TireAudit> {
        val out = mutableListOf<TireAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireAuditStore(): TireAuditStore {
    return MemoryTireAuditStore(mutableMapOf<String, TireAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireAuditStore(
    var inner: TireAuditStore,
    var cache: TtlCache<TireAudit>,
    var hits: Int,
    var misses: Int,
) : TireAuditStore {
    override fun find(code: String): TireAudit? {
        val hit = (cache.get(code) as TireAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireAudit> = inner.list()
}

fun newCachedTireAuditStore(inner: TireAuditStore, ttl: Int, clock: Clock): TireAuditStore {
    return CachedTireAuditStore(
        inner = inner,
        cache = newTtlCache<TireAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire audits. Empty fields match all. */
data class TireAuditQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireAudit>): MutableList<TireAudit> {
        val matched = filter(items) { candidate: TireAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireAudits(store: TireAuditStore, query: TireAuditQuery): MutableList<TireAudit> {
    return query.apply(store.list())
}
