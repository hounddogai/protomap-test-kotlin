package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive swap proposals by code. */
interface AdaptiveSwapProposalStore {
    fun find(code: String): AdaptiveSwapProposal?
    fun save(item: AdaptiveSwapProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSwapProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSwapProposalStore(
    var items: MutableMap<String, AdaptiveSwapProposal>,
    var order: MutableList<String>,
) : AdaptiveSwapProposalStore {
    override fun find(code: String): AdaptiveSwapProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSwapProposal) {
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

    override fun list(): MutableList<AdaptiveSwapProposal> {
        val out = mutableListOf<AdaptiveSwapProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSwapProposalStore(): AdaptiveSwapProposalStore {
    return MemoryAdaptiveSwapProposalStore(
        mutableMapOf<String, AdaptiveSwapProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSwapProposalStore(
    var inner: AdaptiveSwapProposalStore,
    var cache: TtlCache<AdaptiveSwapProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSwapProposalStore {
    override fun find(code: String): AdaptiveSwapProposal? {
        val hit = (cache.get(code) as AdaptiveSwapProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSwapProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSwapProposal> = inner.list()
}

fun newCachedAdaptiveSwapProposalStore(inner: AdaptiveSwapProposalStore, ttl: Int, clock: Clock): AdaptiveSwapProposalStore {
    return CachedAdaptiveSwapProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSwapProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive swap proposals. Empty fields match all. */
data class AdaptiveSwapProposalQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSwapProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSwapProposal>): MutableList<AdaptiveSwapProposal> {
        val matched = filter(items) { candidate: AdaptiveSwapProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSwapProposals(store: AdaptiveSwapProposalStore, query: AdaptiveSwapProposalQuery): MutableList<AdaptiveSwapProposal> {
    return query.apply(store.list())
}
