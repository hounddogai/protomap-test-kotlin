package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched roster audits by code. */
interface BatchedRosterAuditStore {
    fun find(code: String): BatchedRosterAudit?
    fun save(item: BatchedRosterAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRosterAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRosterAuditStore(
    var items: MutableMap<String, BatchedRosterAudit>,
    var order: MutableList<String>,
) : BatchedRosterAuditStore {
    override fun find(code: String): BatchedRosterAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRosterAudit) {
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

    override fun list(): MutableList<BatchedRosterAudit> {
        val out = mutableListOf<BatchedRosterAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRosterAuditStore(): BatchedRosterAuditStore {
    return MemoryBatchedRosterAuditStore(mutableMapOf<String, BatchedRosterAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRosterAuditStore(
    var inner: BatchedRosterAuditStore,
    var cache: TtlCache<BatchedRosterAudit>,
    var hits: Int,
    var misses: Int,
) : BatchedRosterAuditStore {
    override fun find(code: String): BatchedRosterAudit? {
        val hit = (cache.get(code) as BatchedRosterAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRosterAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRosterAudit> = inner.list()
}

fun newCachedBatchedRosterAuditStore(inner: BatchedRosterAuditStore, ttl: Int, clock: Clock): BatchedRosterAuditStore {
    return CachedBatchedRosterAuditStore(
        inner = inner,
        cache = newTtlCache<BatchedRosterAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched roster audits. Empty fields match all. */
data class BatchedRosterAuditQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRosterAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRosterAudit>): MutableList<BatchedRosterAudit> {
        val matched = filter(items) { candidate: BatchedRosterAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRosterAudits(store: BatchedRosterAuditStore, query: BatchedRosterAuditQuery): MutableList<BatchedRosterAudit> {
    return query.apply(store.list())
}
