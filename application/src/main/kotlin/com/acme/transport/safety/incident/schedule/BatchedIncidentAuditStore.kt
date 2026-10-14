package com.acme.transport.safety.incident.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched incident audits by code. */
interface BatchedIncidentAuditStore {
    fun find(code: String): BatchedIncidentAudit?
    fun save(item: BatchedIncidentAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIncidentAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIncidentAuditStore(
    var items: MutableMap<String, BatchedIncidentAudit>,
    var order: MutableList<String>,
) : BatchedIncidentAuditStore {
    override fun find(code: String): BatchedIncidentAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIncidentAudit) {
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

    override fun list(): MutableList<BatchedIncidentAudit> {
        val out = mutableListOf<BatchedIncidentAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIncidentAuditStore(): BatchedIncidentAuditStore {
    return MemoryBatchedIncidentAuditStore(
        mutableMapOf<String, BatchedIncidentAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIncidentAuditStore(
    var inner: BatchedIncidentAuditStore,
    var cache: TtlCache<BatchedIncidentAudit>,
    var hits: Int,
    var misses: Int,
) : BatchedIncidentAuditStore {
    override fun find(code: String): BatchedIncidentAudit? {
        val hit = (cache.get(code) as BatchedIncidentAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIncidentAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIncidentAudit> = inner.list()
}

fun newCachedBatchedIncidentAuditStore(inner: BatchedIncidentAuditStore, ttl: Int, clock: Clock): BatchedIncidentAuditStore {
    return CachedBatchedIncidentAuditStore(
        inner = inner,
        cache = newTtlCache<BatchedIncidentAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched incident audits. Empty fields match all. */
data class BatchedIncidentAuditQuery(
    var source: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIncidentAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<BatchedIncidentAudit>): MutableList<BatchedIncidentAudit> {
        val matched = filter(items) { candidate: BatchedIncidentAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIncidentAudits(store: BatchedIncidentAuditStore, query: BatchedIncidentAuditQuery): MutableList<BatchedIncidentAudit> {
    return query.apply(store.list())
}
