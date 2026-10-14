package com.acme.transport.crewing.swap.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap proposals by code. */
interface SwapProposalStore {
    fun find(code: String): SwapProposal?
    fun save(item: SwapProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapProposalStore(
    var items: MutableMap<String, SwapProposal>,
    var order: MutableList<String>,
) : SwapProposalStore {
    override fun find(code: String): SwapProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapProposal) {
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

    override fun list(): MutableList<SwapProposal> {
        val out = mutableListOf<SwapProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapProposalStore(): SwapProposalStore {
    return MemorySwapProposalStore(mutableMapOf<String, SwapProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapProposalStore(
    var inner: SwapProposalStore,
    var cache: TtlCache<SwapProposal>,
    var hits: Int,
    var misses: Int,
) : SwapProposalStore {
    override fun find(code: String): SwapProposal? {
        val hit = (cache.get(code) as SwapProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapProposal> = inner.list()
}

fun newCachedSwapProposalStore(inner: SwapProposalStore, ttl: Int, clock: Clock): SwapProposalStore {
    return CachedSwapProposalStore(
        inner = inner,
        cache = newTtlCache<SwapProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap proposals. Empty fields match all. */
data class SwapProposalQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapProposal): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapProposal>): MutableList<SwapProposal> {
        val matched = filter(items) { candidate: SwapProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapProposals(store: SwapProposalStore, query: SwapProposalQuery): MutableList<SwapProposal> {
    return query.apply(store.list())
}
