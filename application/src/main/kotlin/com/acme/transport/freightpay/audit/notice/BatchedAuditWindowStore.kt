package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched audit windows by code. */
interface BatchedAuditWindowStore {
    fun find(code: String): BatchedAuditWindow?
    fun save(item: BatchedAuditWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAuditWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAuditWindowStore(
    var items: MutableMap<String, BatchedAuditWindow>,
    var order: MutableList<String>,
) : BatchedAuditWindowStore {
    override fun find(code: String): BatchedAuditWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAuditWindow) {
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

    override fun list(): MutableList<BatchedAuditWindow> {
        val out = mutableListOf<BatchedAuditWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAuditWindowStore(): BatchedAuditWindowStore {
    return MemoryBatchedAuditWindowStore(mutableMapOf<String, BatchedAuditWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAuditWindowStore(
    var inner: BatchedAuditWindowStore,
    var cache: TtlCache<BatchedAuditWindow>,
    var hits: Int,
    var misses: Int,
) : BatchedAuditWindowStore {
    override fun find(code: String): BatchedAuditWindow? {
        val hit = (cache.get(code) as BatchedAuditWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAuditWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAuditWindow> = inner.list()
}

fun newCachedBatchedAuditWindowStore(inner: BatchedAuditWindowStore, ttl: Int, clock: Clock): BatchedAuditWindowStore {
    return CachedBatchedAuditWindowStore(
        inner = inner,
        cache = newTtlCache<BatchedAuditWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched audit windows. Empty fields match all. */
data class BatchedAuditWindowQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAuditWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAuditWindow>): MutableList<BatchedAuditWindow> {
        val matched = filter(items) { candidate: BatchedAuditWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAuditWindows(store: BatchedAuditWindowStore, query: BatchedAuditWindowQuery): MutableList<BatchedAuditWindow> {
    return query.apply(store.list())
}
