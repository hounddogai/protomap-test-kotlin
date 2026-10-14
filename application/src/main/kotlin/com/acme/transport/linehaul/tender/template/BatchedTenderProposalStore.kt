package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched tender proposals by code. */
interface BatchedTenderProposalStore {
    fun find(code: String): BatchedTenderProposal?
    fun save(item: BatchedTenderProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTenderProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTenderProposalStore(
    var items: MutableMap<String, BatchedTenderProposal>,
    var order: MutableList<String>,
) : BatchedTenderProposalStore {
    override fun find(code: String): BatchedTenderProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTenderProposal) {
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

    override fun list(): MutableList<BatchedTenderProposal> {
        val out = mutableListOf<BatchedTenderProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTenderProposalStore(): BatchedTenderProposalStore {
    return MemoryBatchedTenderProposalStore(
        mutableMapOf<String, BatchedTenderProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTenderProposalStore(
    var inner: BatchedTenderProposalStore,
    var cache: TtlCache<BatchedTenderProposal>,
    var hits: Int,
    var misses: Int,
) : BatchedTenderProposalStore {
    override fun find(code: String): BatchedTenderProposal? {
        val hit = (cache.get(code) as BatchedTenderProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTenderProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTenderProposal> = inner.list()
}

fun newCachedBatchedTenderProposalStore(inner: BatchedTenderProposalStore, ttl: Int, clock: Clock): BatchedTenderProposalStore {
    return CachedBatchedTenderProposalStore(
        inner = inner,
        cache = newTtlCache<BatchedTenderProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tender proposals. Empty fields match all. */
data class BatchedTenderProposalQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTenderProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTenderProposal>): MutableList<BatchedTenderProposal> {
        val matched = filter(items) { candidate: BatchedTenderProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTenderProposals(store: BatchedTenderProposalStore, query: BatchedTenderProposalQuery): MutableList<BatchedTenderProposal> {
    return query.apply(store.list())
}
