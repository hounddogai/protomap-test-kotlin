package com.acme.transport.freightpay.audit.auditrollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending audit batches by code. */
interface PendingAuditBatchStore {
    fun find(code: String): PendingAuditBatch?
    fun save(item: PendingAuditBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAuditBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAuditBatchStore(
    var items: MutableMap<String, PendingAuditBatch>,
    var order: MutableList<String>,
) : PendingAuditBatchStore {
    override fun find(code: String): PendingAuditBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAuditBatch) {
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

    override fun list(): MutableList<PendingAuditBatch> {
        val out = mutableListOf<PendingAuditBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAuditBatchStore(): PendingAuditBatchStore {
    return MemoryPendingAuditBatchStore(mutableMapOf<String, PendingAuditBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAuditBatchStore(
    var inner: PendingAuditBatchStore,
    var cache: TtlCache<PendingAuditBatch>,
    var hits: Int,
    var misses: Int,
) : PendingAuditBatchStore {
    override fun find(code: String): PendingAuditBatch? {
        val hit = (cache.get(code) as PendingAuditBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAuditBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAuditBatch> = inner.list()
}

fun newCachedPendingAuditBatchStore(inner: PendingAuditBatchStore, ttl: Int, clock: Clock): PendingAuditBatchStore {
    return CachedPendingAuditBatchStore(
        inner = inner,
        cache = newTtlCache<PendingAuditBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending audit batches. Empty fields match all. */
data class PendingAuditBatchQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAuditBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAuditBatch>): MutableList<PendingAuditBatch> {
        val matched = filter(items) { candidate: PendingAuditBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAuditBatches(store: PendingAuditBatchStore, query: PendingAuditBatchQuery): MutableList<PendingAuditBatch> {
    return query.apply(store.list())
}
