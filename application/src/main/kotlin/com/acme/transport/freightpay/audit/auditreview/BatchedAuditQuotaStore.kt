package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched audit quotas by code. */
interface BatchedAuditQuotaStore {
    fun find(code: String): BatchedAuditQuota?
    fun save(item: BatchedAuditQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAuditQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAuditQuotaStore(
    var items: MutableMap<String, BatchedAuditQuota>,
    var order: MutableList<String>,
) : BatchedAuditQuotaStore {
    override fun find(code: String): BatchedAuditQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAuditQuota) {
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

    override fun list(): MutableList<BatchedAuditQuota> {
        val out = mutableListOf<BatchedAuditQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAuditQuotaStore(): BatchedAuditQuotaStore {
    return MemoryBatchedAuditQuotaStore(mutableMapOf<String, BatchedAuditQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAuditQuotaStore(
    var inner: BatchedAuditQuotaStore,
    var cache: TtlCache<BatchedAuditQuota>,
    var hits: Int,
    var misses: Int,
) : BatchedAuditQuotaStore {
    override fun find(code: String): BatchedAuditQuota? {
        val hit = (cache.get(code) as BatchedAuditQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAuditQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAuditQuota> = inner.list()
}

fun newCachedBatchedAuditQuotaStore(inner: BatchedAuditQuotaStore, ttl: Int, clock: Clock): BatchedAuditQuotaStore {
    return CachedBatchedAuditQuotaStore(
        inner = inner,
        cache = newTtlCache<BatchedAuditQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched audit quotas. Empty fields match all. */
data class BatchedAuditQuotaQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAuditQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAuditQuota>): MutableList<BatchedAuditQuota> {
        val matched = filter(items) { candidate: BatchedAuditQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAuditQuotas(store: BatchedAuditQuotaStore, query: BatchedAuditQuotaQuery): MutableList<BatchedAuditQuota> {
    return query.apply(store.list())
}
