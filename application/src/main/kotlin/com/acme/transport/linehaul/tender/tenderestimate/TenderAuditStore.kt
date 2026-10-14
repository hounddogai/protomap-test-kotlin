package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender audits by code. */
interface TenderAuditStore {
    fun find(code: String): TenderAudit?
    fun save(item: TenderAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderAuditStore(
    var items: MutableMap<String, TenderAudit>,
    var order: MutableList<String>,
) : TenderAuditStore {
    override fun find(code: String): TenderAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderAudit) {
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

    override fun list(): MutableList<TenderAudit> {
        val out = mutableListOf<TenderAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderAuditStore(): TenderAuditStore {
    return MemoryTenderAuditStore(mutableMapOf<String, TenderAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderAuditStore(
    var inner: TenderAuditStore,
    var cache: TtlCache<TenderAudit>,
    var hits: Int,
    var misses: Int,
) : TenderAuditStore {
    override fun find(code: String): TenderAudit? {
        val hit = (cache.get(code) as TenderAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderAudit> = inner.list()
}

fun newCachedTenderAuditStore(inner: TenderAuditStore, ttl: Int, clock: Clock): TenderAuditStore {
    return CachedTenderAuditStore(
        inner = inner,
        cache = newTtlCache<TenderAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender audits. Empty fields match all. */
data class TenderAuditQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderAudit): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderAudit>): MutableList<TenderAudit> {
        val matched = filter(items) { candidate: TenderAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderAudits(store: TenderAuditStore, query: TenderAuditQuery): MutableList<TenderAudit> {
    return query.apply(store.list())
}
