package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary audit batches by code. */
interface PrimaryAuditBatchStore {
    fun find(code: String): PrimaryAuditBatch?
    fun save(item: PrimaryAuditBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAuditBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAuditBatchStore(
    var items: MutableMap<String, PrimaryAuditBatch>,
    var order: MutableList<String>,
) : PrimaryAuditBatchStore {
    override fun find(code: String): PrimaryAuditBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAuditBatch) {
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

    override fun list(): MutableList<PrimaryAuditBatch> {
        val out = mutableListOf<PrimaryAuditBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAuditBatchStore(): PrimaryAuditBatchStore {
    return MemoryPrimaryAuditBatchStore(mutableMapOf<String, PrimaryAuditBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAuditBatchStore(
    var inner: PrimaryAuditBatchStore,
    var cache: TtlCache<PrimaryAuditBatch>,
    var hits: Int,
    var misses: Int,
) : PrimaryAuditBatchStore {
    override fun find(code: String): PrimaryAuditBatch? {
        val hit = (cache.get(code) as PrimaryAuditBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAuditBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAuditBatch> = inner.list()
}

fun newCachedPrimaryAuditBatchStore(inner: PrimaryAuditBatchStore, ttl: Int, clock: Clock): PrimaryAuditBatchStore {
    return CachedPrimaryAuditBatchStore(
        inner = inner,
        cache = newTtlCache<PrimaryAuditBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary audit batches. Empty fields match all. */
data class PrimaryAuditBatchQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAuditBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAuditBatch>): MutableList<PrimaryAuditBatch> {
        val matched = filter(items) { candidate: PrimaryAuditBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAuditBatches(store: PrimaryAuditBatchStore, query: PrimaryAuditBatchQuery): MutableList<PrimaryAuditBatch> {
    return query.apply(store.list())
}
