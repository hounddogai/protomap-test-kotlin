package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched incident proposals by code. */
interface BatchedIncidentProposalStore {
    fun find(code: String): BatchedIncidentProposal?
    fun save(item: BatchedIncidentProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIncidentProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIncidentProposalStore(
    var items: MutableMap<String, BatchedIncidentProposal>,
    var order: MutableList<String>,
) : BatchedIncidentProposalStore {
    override fun find(code: String): BatchedIncidentProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIncidentProposal) {
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

    override fun list(): MutableList<BatchedIncidentProposal> {
        val out = mutableListOf<BatchedIncidentProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIncidentProposalStore(): BatchedIncidentProposalStore {
    return MemoryBatchedIncidentProposalStore(
        mutableMapOf<String, BatchedIncidentProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIncidentProposalStore(
    var inner: BatchedIncidentProposalStore,
    var cache: TtlCache<BatchedIncidentProposal>,
    var hits: Int,
    var misses: Int,
) : BatchedIncidentProposalStore {
    override fun find(code: String): BatchedIncidentProposal? {
        val hit = (cache.get(code) as BatchedIncidentProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIncidentProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIncidentProposal> = inner.list()
}

fun newCachedBatchedIncidentProposalStore(inner: BatchedIncidentProposalStore, ttl: Int, clock: Clock): BatchedIncidentProposalStore {
    return CachedBatchedIncidentProposalStore(
        inner = inner,
        cache = newTtlCache<BatchedIncidentProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched incident proposals. Empty fields match all. */
data class BatchedIncidentProposalQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIncidentProposal): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedIncidentProposal>): MutableList<BatchedIncidentProposal> {
        val matched = filter(items) { candidate: BatchedIncidentProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIncidentProposals(store: BatchedIncidentProposalStore, query: BatchedIncidentProposalQuery): MutableList<BatchedIncidentProposal> {
    return query.apply(store.list())
}
