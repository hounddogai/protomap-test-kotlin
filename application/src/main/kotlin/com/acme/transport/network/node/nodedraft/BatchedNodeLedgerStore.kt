package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node ledgers by code. */
interface BatchedNodeLedgerStore {
    fun find(code: String): BatchedNodeLedger?
    fun save(item: BatchedNodeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodeLedgerStore(
    var items: MutableMap<String, BatchedNodeLedger>,
    var order: MutableList<String>,
) : BatchedNodeLedgerStore {
    override fun find(code: String): BatchedNodeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodeLedger) {
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

    override fun list(): MutableList<BatchedNodeLedger> {
        val out = mutableListOf<BatchedNodeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodeLedgerStore(): BatchedNodeLedgerStore {
    return MemoryBatchedNodeLedgerStore(mutableMapOf<String, BatchedNodeLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodeLedgerStore(
    var inner: BatchedNodeLedgerStore,
    var cache: TtlCache<BatchedNodeLedger>,
    var hits: Int,
    var misses: Int,
) : BatchedNodeLedgerStore {
    override fun find(code: String): BatchedNodeLedger? {
        val hit = (cache.get(code) as BatchedNodeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodeLedger> = inner.list()
}

fun newCachedBatchedNodeLedgerStore(inner: BatchedNodeLedgerStore, ttl: Int, clock: Clock): BatchedNodeLedgerStore {
    return CachedBatchedNodeLedgerStore(
        inner = inner,
        cache = newTtlCache<BatchedNodeLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node ledgers. Empty fields match all. */
data class BatchedNodeLedgerQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodeLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNodeLedger>): MutableList<BatchedNodeLedger> {
        val matched = filter(items) { candidate: BatchedNodeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodeLedgers(store: BatchedNodeLedgerStore, query: BatchedNodeLedgerQuery): MutableList<BatchedNodeLedger> {
    return query.apply(store.list())
}
