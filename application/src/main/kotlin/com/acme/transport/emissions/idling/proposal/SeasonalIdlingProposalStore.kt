package com.acme.transport.emissions.idling.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal idling proposals by code. */
interface SeasonalIdlingProposalStore {
    fun find(code: String): SeasonalIdlingProposal?
    fun save(item: SeasonalIdlingProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalIdlingProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalIdlingProposalStore(
    var items: MutableMap<String, SeasonalIdlingProposal>,
    var order: MutableList<String>,
) : SeasonalIdlingProposalStore {
    override fun find(code: String): SeasonalIdlingProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalIdlingProposal) {
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

    override fun list(): MutableList<SeasonalIdlingProposal> {
        val out = mutableListOf<SeasonalIdlingProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalIdlingProposalStore(): SeasonalIdlingProposalStore {
    return MemorySeasonalIdlingProposalStore(
        mutableMapOf<String, SeasonalIdlingProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalIdlingProposalStore(
    var inner: SeasonalIdlingProposalStore,
    var cache: TtlCache<SeasonalIdlingProposal>,
    var hits: Int,
    var misses: Int,
) : SeasonalIdlingProposalStore {
    override fun find(code: String): SeasonalIdlingProposal? {
        val hit = (cache.get(code) as SeasonalIdlingProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalIdlingProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalIdlingProposal> = inner.list()
}

fun newCachedSeasonalIdlingProposalStore(inner: SeasonalIdlingProposalStore, ttl: Int, clock: Clock): SeasonalIdlingProposalStore {
    return CachedSeasonalIdlingProposalStore(
        inner = inner,
        cache = newTtlCache<SeasonalIdlingProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal idling proposals. Empty fields match all. */
data class SeasonalIdlingProposalQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalIdlingProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalIdlingProposal>): MutableList<SeasonalIdlingProposal> {
        val matched = filter(items) { candidate: SeasonalIdlingProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalIdlingProposals(store: SeasonalIdlingProposalStore, query: SeasonalIdlingProposalQuery): MutableList<SeasonalIdlingProposal> {
    return query.apply(store.list())
}
