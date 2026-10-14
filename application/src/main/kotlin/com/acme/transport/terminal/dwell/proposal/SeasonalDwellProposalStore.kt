package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dwell proposals by code. */
interface SeasonalDwellProposalStore {
    fun find(code: String): SeasonalDwellProposal?
    fun save(item: SeasonalDwellProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDwellProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDwellProposalStore(
    var items: MutableMap<String, SeasonalDwellProposal>,
    var order: MutableList<String>,
) : SeasonalDwellProposalStore {
    override fun find(code: String): SeasonalDwellProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDwellProposal) {
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

    override fun list(): MutableList<SeasonalDwellProposal> {
        val out = mutableListOf<SeasonalDwellProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDwellProposalStore(): SeasonalDwellProposalStore {
    return MemorySeasonalDwellProposalStore(
        mutableMapOf<String, SeasonalDwellProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDwellProposalStore(
    var inner: SeasonalDwellProposalStore,
    var cache: TtlCache<SeasonalDwellProposal>,
    var hits: Int,
    var misses: Int,
) : SeasonalDwellProposalStore {
    override fun find(code: String): SeasonalDwellProposal? {
        val hit = (cache.get(code) as SeasonalDwellProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDwellProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDwellProposal> = inner.list()
}

fun newCachedSeasonalDwellProposalStore(inner: SeasonalDwellProposalStore, ttl: Int, clock: Clock): SeasonalDwellProposalStore {
    return CachedSeasonalDwellProposalStore(
        inner = inner,
        cache = newTtlCache<SeasonalDwellProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dwell proposals. Empty fields match all. */
data class SeasonalDwellProposalQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDwellProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDwellProposal>): MutableList<SeasonalDwellProposal> {
        val matched = filter(items) { candidate: SeasonalDwellProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDwellProposals(store: SeasonalDwellProposalStore, query: SeasonalDwellProposalQuery): MutableList<SeasonalDwellProposal> {
    return query.apply(store.list())
}
