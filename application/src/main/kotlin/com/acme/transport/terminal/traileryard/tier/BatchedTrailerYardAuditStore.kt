package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched trailer yard audits by code. */
interface BatchedTrailerYardAuditStore {
    fun find(code: String): BatchedTrailerYardAudit?
    fun save(item: BatchedTrailerYardAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerYardAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerYardAuditStore(
    var items: MutableMap<String, BatchedTrailerYardAudit>,
    var order: MutableList<String>,
) : BatchedTrailerYardAuditStore {
    override fun find(code: String): BatchedTrailerYardAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerYardAudit) {
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

    override fun list(): MutableList<BatchedTrailerYardAudit> {
        val out = mutableListOf<BatchedTrailerYardAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerYardAuditStore(): BatchedTrailerYardAuditStore {
    return MemoryBatchedTrailerYardAuditStore(
        mutableMapOf<String, BatchedTrailerYardAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerYardAuditStore(
    var inner: BatchedTrailerYardAuditStore,
    var cache: TtlCache<BatchedTrailerYardAudit>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerYardAuditStore {
    override fun find(code: String): BatchedTrailerYardAudit? {
        val hit = (cache.get(code) as BatchedTrailerYardAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerYardAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerYardAudit> = inner.list()
}

fun newCachedBatchedTrailerYardAuditStore(inner: BatchedTrailerYardAuditStore, ttl: Int, clock: Clock): BatchedTrailerYardAuditStore {
    return CachedBatchedTrailerYardAuditStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerYardAudit>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer yard audits. Empty fields match all. */
data class BatchedTrailerYardAuditQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerYardAudit): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTrailerYardAudit>): MutableList<BatchedTrailerYardAudit> {
        val matched = filter(items) { candidate: BatchedTrailerYardAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerYardAudits(store: BatchedTrailerYardAuditStore, query: BatchedTrailerYardAuditQuery): MutableList<BatchedTrailerYardAudit> {
    return query.apply(store.list())
}
