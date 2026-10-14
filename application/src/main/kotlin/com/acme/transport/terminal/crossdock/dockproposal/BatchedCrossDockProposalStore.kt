package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched cross dock proposals by code. */
interface BatchedCrossDockProposalStore {
    fun find(code: String): BatchedCrossDockProposal?
    fun save(item: BatchedCrossDockProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCrossDockProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCrossDockProposalStore(
    var items: MutableMap<String, BatchedCrossDockProposal>,
    var order: MutableList<String>,
) : BatchedCrossDockProposalStore {
    override fun find(code: String): BatchedCrossDockProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCrossDockProposal) {
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

    override fun list(): MutableList<BatchedCrossDockProposal> {
        val out = mutableListOf<BatchedCrossDockProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCrossDockProposalStore(): BatchedCrossDockProposalStore {
    return MemoryBatchedCrossDockProposalStore(
        mutableMapOf<String, BatchedCrossDockProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCrossDockProposalStore(
    var inner: BatchedCrossDockProposalStore,
    var cache: TtlCache<BatchedCrossDockProposal>,
    var hits: Int,
    var misses: Int,
) : BatchedCrossDockProposalStore {
    override fun find(code: String): BatchedCrossDockProposal? {
        val hit = (cache.get(code) as BatchedCrossDockProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCrossDockProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCrossDockProposal> = inner.list()
}

fun newCachedBatchedCrossDockProposalStore(inner: BatchedCrossDockProposalStore, ttl: Int, clock: Clock): BatchedCrossDockProposalStore {
    return CachedBatchedCrossDockProposalStore(
        inner = inner,
        cache = newTtlCache<BatchedCrossDockProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cross dock proposals. Empty fields match all. */
data class BatchedCrossDockProposalQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCrossDockProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCrossDockProposal>): MutableList<BatchedCrossDockProposal> {
        val matched = filter(items) { candidate: BatchedCrossDockProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCrossDockProposals(store: BatchedCrossDockProposalStore, query: BatchedCrossDockProposalQuery): MutableList<BatchedCrossDockProposal> {
    return query.apply(store.list())
}
