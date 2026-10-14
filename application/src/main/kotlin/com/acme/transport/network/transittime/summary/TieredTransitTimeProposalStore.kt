package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered transit time proposals by code. */
interface TieredTransitTimeProposalStore {
    fun find(code: String): TieredTransitTimeProposal?
    fun save(item: TieredTransitTimeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTransitTimeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTransitTimeProposalStore(
    var items: MutableMap<String, TieredTransitTimeProposal>,
    var order: MutableList<String>,
) : TieredTransitTimeProposalStore {
    override fun find(code: String): TieredTransitTimeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTransitTimeProposal) {
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

    override fun list(): MutableList<TieredTransitTimeProposal> {
        val out = mutableListOf<TieredTransitTimeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTransitTimeProposalStore(): TieredTransitTimeProposalStore {
    return MemoryTieredTransitTimeProposalStore(
        mutableMapOf<String, TieredTransitTimeProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTransitTimeProposalStore(
    var inner: TieredTransitTimeProposalStore,
    var cache: TtlCache<TieredTransitTimeProposal>,
    var hits: Int,
    var misses: Int,
) : TieredTransitTimeProposalStore {
    override fun find(code: String): TieredTransitTimeProposal? {
        val hit = (cache.get(code) as TieredTransitTimeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTransitTimeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTransitTimeProposal> = inner.list()
}

fun newCachedTieredTransitTimeProposalStore(inner: TieredTransitTimeProposalStore, ttl: Int, clock: Clock): TieredTransitTimeProposalStore {
    return CachedTieredTransitTimeProposalStore(
        inner = inner,
        cache = newTtlCache<TieredTransitTimeProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered transit time proposals. Empty fields match all. */
data class TieredTransitTimeProposalQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTransitTimeProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<TieredTransitTimeProposal>): MutableList<TieredTransitTimeProposal> {
        val matched = filter(items) { candidate: TieredTransitTimeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTransitTimeProposals(store: TieredTransitTimeProposalStore, query: TieredTransitTimeProposalQuery): MutableList<TieredTransitTimeProposal> {
    return query.apply(store.list())
}
