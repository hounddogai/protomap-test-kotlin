package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard ledgers by code. */
interface BatchedScorecardLedgerStore {
    fun find(code: String): BatchedScorecardLedger?
    fun save(item: BatchedScorecardLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardLedgerStore(
    var items: MutableMap<String, BatchedScorecardLedger>,
    var order: MutableList<String>,
) : BatchedScorecardLedgerStore {
    override fun find(code: String): BatchedScorecardLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardLedger) {
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

    override fun list(): MutableList<BatchedScorecardLedger> {
        val out = mutableListOf<BatchedScorecardLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardLedgerStore(): BatchedScorecardLedgerStore {
    return MemoryBatchedScorecardLedgerStore(
        mutableMapOf<String, BatchedScorecardLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardLedgerStore(
    var inner: BatchedScorecardLedgerStore,
    var cache: TtlCache<BatchedScorecardLedger>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardLedgerStore {
    override fun find(code: String): BatchedScorecardLedger? {
        val hit = (cache.get(code) as BatchedScorecardLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardLedger> = inner.list()
}

fun newCachedBatchedScorecardLedgerStore(inner: BatchedScorecardLedgerStore, ttl: Int, clock: Clock): BatchedScorecardLedgerStore {
    return CachedBatchedScorecardLedgerStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard ledgers. Empty fields match all. */
data class BatchedScorecardLedgerQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BatchedScorecardLedger>): MutableList<BatchedScorecardLedger> {
        val matched = filter(items) { candidate: BatchedScorecardLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardLedgers(store: BatchedScorecardLedgerStore, query: BatchedScorecardLedgerQuery): MutableList<BatchedScorecardLedger> {
    return query.apply(store.list())
}
