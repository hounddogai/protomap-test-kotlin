package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit batches by code. */
interface AuditBatchStore {
    fun find(code: String): AuditBatch?
    fun save(item: AuditBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditBatchStore(
    var items: MutableMap<String, AuditBatch>,
    var order: MutableList<String>,
) : AuditBatchStore {
    override fun find(code: String): AuditBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditBatch) {
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

    override fun list(): MutableList<AuditBatch> {
        val out = mutableListOf<AuditBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditBatchStore(): AuditBatchStore {
    return MemoryAuditBatchStore(mutableMapOf<String, AuditBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditBatchStore(
    var inner: AuditBatchStore,
    var cache: TtlCache<AuditBatch>,
    var hits: Int,
    var misses: Int,
) : AuditBatchStore {
    override fun find(code: String): AuditBatch? {
        val hit = (cache.get(code) as AuditBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditBatch> = inner.list()
}

fun newCachedAuditBatchStore(inner: AuditBatchStore, ttl: Int, clock: Clock): AuditBatchStore {
    return CachedAuditBatchStore(
        inner = inner,
        cache = newTtlCache<AuditBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit batches. Empty fields match all. */
data class AuditBatchQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditBatch): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditBatch>): MutableList<AuditBatch> {
        val matched = filter(items) { candidate: AuditBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditBatches(store: AuditBatchStore, query: AuditBatchQuery): MutableList<AuditBatch> {
    return query.apply(store.list())
}
