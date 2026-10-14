package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient lease return audits by code. */
interface LenientLeaseReturnAuditStore {
    fun find(code: String): LenientLeaseReturnAudit?
    fun save(item: LenientLeaseReturnAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientLeaseReturnAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientLeaseReturnAuditStore(
    var items: MutableMap<String, LenientLeaseReturnAudit>,
    var order: MutableList<String>,
) : LenientLeaseReturnAuditStore {
    override fun find(code: String): LenientLeaseReturnAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientLeaseReturnAudit) {
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

    override fun list(): MutableList<LenientLeaseReturnAudit> {
        val out = mutableListOf<LenientLeaseReturnAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientLeaseReturnAuditStore(): LenientLeaseReturnAuditStore {
    return MemoryLenientLeaseReturnAuditStore(
        mutableMapOf<String, LenientLeaseReturnAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientLeaseReturnAuditStore(
    var inner: LenientLeaseReturnAuditStore,
    var cache: TtlCache<LenientLeaseReturnAudit>,
    var hits: Int,
    var misses: Int,
) : LenientLeaseReturnAuditStore {
    override fun find(code: String): LenientLeaseReturnAudit? {
        val hit = (cache.get(code) as LenientLeaseReturnAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientLeaseReturnAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientLeaseReturnAudit> = inner.list()
}

fun newCachedLenientLeaseReturnAuditStore(inner: LenientLeaseReturnAuditStore, ttl: Int, clock: Clock): LenientLeaseReturnAuditStore {
    return CachedLenientLeaseReturnAuditStore(
        inner = inner,
        cache = newTtlCache<LenientLeaseReturnAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient lease return audits. Empty fields match all. */
data class LenientLeaseReturnAuditQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientLeaseReturnAudit): Boolean {
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

    fun apply(items: MutableList<LenientLeaseReturnAudit>): MutableList<LenientLeaseReturnAudit> {
        val matched = filter(items) { candidate: LenientLeaseReturnAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientLeaseReturnAudits(store: LenientLeaseReturnAuditStore, query: LenientLeaseReturnAuditQuery): MutableList<LenientLeaseReturnAudit> {
    return query.apply(store.list())
}
