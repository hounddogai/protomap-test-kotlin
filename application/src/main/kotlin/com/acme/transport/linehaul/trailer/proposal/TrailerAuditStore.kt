package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer audits by code. */
interface TrailerAuditStore {
    fun find(code: String): TrailerAudit?
    fun save(item: TrailerAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerAuditStore(
    var items: MutableMap<String, TrailerAudit>,
    var order: MutableList<String>,
) : TrailerAuditStore {
    override fun find(code: String): TrailerAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerAudit) {
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

    override fun list(): MutableList<TrailerAudit> {
        val out = mutableListOf<TrailerAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerAuditStore(): TrailerAuditStore {
    return MemoryTrailerAuditStore(mutableMapOf<String, TrailerAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerAuditStore(
    var inner: TrailerAuditStore,
    var cache: TtlCache<TrailerAudit>,
    var hits: Int,
    var misses: Int,
) : TrailerAuditStore {
    override fun find(code: String): TrailerAudit? {
        val hit = (cache.get(code) as TrailerAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerAudit> = inner.list()
}

fun newCachedTrailerAuditStore(inner: TrailerAuditStore, ttl: Int, clock: Clock): TrailerAuditStore {
    return CachedTrailerAuditStore(
        inner = inner,
        cache = newTtlCache<TrailerAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer audits. Empty fields match all. */
data class TrailerAuditQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerAudit): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerAudit>): MutableList<TrailerAudit> {
        val matched = filter(items) { candidate: TrailerAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerAudits(store: TrailerAuditStore, query: TrailerAuditQuery): MutableList<TrailerAudit> {
    return query.apply(store.list())
}
