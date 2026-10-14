package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return audits by code. */
interface LeaseReturnAuditStore {
    fun find(code: String): LeaseReturnAudit?
    fun save(item: LeaseReturnAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnAuditStore(
    var items: MutableMap<String, LeaseReturnAudit>,
    var order: MutableList<String>,
) : LeaseReturnAuditStore {
    override fun find(code: String): LeaseReturnAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnAudit) {
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

    override fun list(): MutableList<LeaseReturnAudit> {
        val out = mutableListOf<LeaseReturnAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnAuditStore(): LeaseReturnAuditStore {
    return MemoryLeaseReturnAuditStore(mutableMapOf<String, LeaseReturnAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnAuditStore(
    var inner: LeaseReturnAuditStore,
    var cache: TtlCache<LeaseReturnAudit>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnAuditStore {
    override fun find(code: String): LeaseReturnAudit? {
        val hit = (cache.get(code) as LeaseReturnAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnAudit> = inner.list()
}

fun newCachedLeaseReturnAuditStore(inner: LeaseReturnAuditStore, ttl: Int, clock: Clock): LeaseReturnAuditStore {
    return CachedLeaseReturnAuditStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return audits. Empty fields match all. */
data class LeaseReturnAuditQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnAudit>): MutableList<LeaseReturnAudit> {
        val matched = filter(items) { candidate: LeaseReturnAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnAudits(store: LeaseReturnAuditStore, query: LeaseReturnAuditQuery): MutableList<LeaseReturnAudit> {
    return query.apply(store.list())
}
