package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched defect proposals by code. */
interface BatchedDefectProposalStore {
    fun find(code: String): BatchedDefectProposal?
    fun save(item: BatchedDefectProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDefectProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDefectProposalStore(
    var items: MutableMap<String, BatchedDefectProposal>,
    var order: MutableList<String>,
) : BatchedDefectProposalStore {
    override fun find(code: String): BatchedDefectProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDefectProposal) {
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

    override fun list(): MutableList<BatchedDefectProposal> {
        val out = mutableListOf<BatchedDefectProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDefectProposalStore(): BatchedDefectProposalStore {
    return MemoryBatchedDefectProposalStore(
        mutableMapOf<String, BatchedDefectProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDefectProposalStore(
    var inner: BatchedDefectProposalStore,
    var cache: TtlCache<BatchedDefectProposal>,
    var hits: Int,
    var misses: Int,
) : BatchedDefectProposalStore {
    override fun find(code: String): BatchedDefectProposal? {
        val hit = (cache.get(code) as BatchedDefectProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDefectProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDefectProposal> = inner.list()
}

fun newCachedBatchedDefectProposalStore(inner: BatchedDefectProposalStore, ttl: Int, clock: Clock): BatchedDefectProposalStore {
    return CachedBatchedDefectProposalStore(
        inner = inner,
        cache = newTtlCache<BatchedDefectProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched defect proposals. Empty fields match all. */
data class BatchedDefectProposalQuery(
    var country: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDefectProposal): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDefectProposal>): MutableList<BatchedDefectProposal> {
        val matched = filter(items) { candidate: BatchedDefectProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDefectProposals(store: BatchedDefectProposalStore, query: BatchedDefectProposalQuery): MutableList<BatchedDefectProposal> {
    return query.apply(store.list())
}
