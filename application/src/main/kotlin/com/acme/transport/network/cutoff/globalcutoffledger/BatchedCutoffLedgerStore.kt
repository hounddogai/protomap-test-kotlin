package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched cutoff ledgers by code. */
interface BatchedCutoffLedgerStore {
    fun find(code: String): BatchedCutoffLedger?
    fun save(item: BatchedCutoffLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCutoffLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCutoffLedgerStore(
    var items: MutableMap<String, BatchedCutoffLedger>,
    var order: MutableList<String>,
) : BatchedCutoffLedgerStore {
    override fun find(code: String): BatchedCutoffLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCutoffLedger) {
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

    override fun list(): MutableList<BatchedCutoffLedger> {
        val out = mutableListOf<BatchedCutoffLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCutoffLedgerStore(): BatchedCutoffLedgerStore {
    return MemoryBatchedCutoffLedgerStore(mutableMapOf<String, BatchedCutoffLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCutoffLedgerStore(
    var inner: BatchedCutoffLedgerStore,
    var cache: TtlCache<BatchedCutoffLedger>,
    var hits: Int,
    var misses: Int,
) : BatchedCutoffLedgerStore {
    override fun find(code: String): BatchedCutoffLedger? {
        val hit = (cache.get(code) as BatchedCutoffLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCutoffLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCutoffLedger> = inner.list()
}

fun newCachedBatchedCutoffLedgerStore(inner: BatchedCutoffLedgerStore, ttl: Int, clock: Clock): BatchedCutoffLedgerStore {
    return CachedBatchedCutoffLedgerStore(
        inner = inner,
        cache = newTtlCache<BatchedCutoffLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cutoff ledgers. Empty fields match all. */
data class BatchedCutoffLedgerQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCutoffLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<BatchedCutoffLedger>): MutableList<BatchedCutoffLedger> {
        val matched = filter(items) { candidate: BatchedCutoffLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCutoffLedgers(store: BatchedCutoffLedgerStore, query: BatchedCutoffLedgerQuery): MutableList<BatchedCutoffLedger> {
    return query.apply(store.list())
}
