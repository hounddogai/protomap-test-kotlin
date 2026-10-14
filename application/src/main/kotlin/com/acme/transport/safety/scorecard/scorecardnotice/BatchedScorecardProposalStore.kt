package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard proposals by code. */
interface BatchedScorecardProposalStore {
    fun find(code: String): BatchedScorecardProposal?
    fun save(item: BatchedScorecardProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardProposalStore(
    var items: MutableMap<String, BatchedScorecardProposal>,
    var order: MutableList<String>,
) : BatchedScorecardProposalStore {
    override fun find(code: String): BatchedScorecardProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardProposal) {
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

    override fun list(): MutableList<BatchedScorecardProposal> {
        val out = mutableListOf<BatchedScorecardProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardProposalStore(): BatchedScorecardProposalStore {
    return MemoryBatchedScorecardProposalStore(
        mutableMapOf<String, BatchedScorecardProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardProposalStore(
    var inner: BatchedScorecardProposalStore,
    var cache: TtlCache<BatchedScorecardProposal>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardProposalStore {
    override fun find(code: String): BatchedScorecardProposal? {
        val hit = (cache.get(code) as BatchedScorecardProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardProposal> = inner.list()
}

fun newCachedBatchedScorecardProposalStore(inner: BatchedScorecardProposalStore, ttl: Int, clock: Clock): BatchedScorecardProposalStore {
    return CachedBatchedScorecardProposalStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard proposals. Empty fields match all. */
data class BatchedScorecardProposalQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardProposal>): MutableList<BatchedScorecardProposal> {
        val matched = filter(items) { candidate: BatchedScorecardProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardProposals(store: BatchedScorecardProposalStore, query: BatchedScorecardProposalQuery): MutableList<BatchedScorecardProposal> {
    return query.apply(store.list())
}
