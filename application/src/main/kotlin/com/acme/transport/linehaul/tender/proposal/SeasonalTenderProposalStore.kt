package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal tender proposals by code. */
interface SeasonalTenderProposalStore {
    fun find(code: String): SeasonalTenderProposal?
    fun save(item: SeasonalTenderProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTenderProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTenderProposalStore(
    var items: MutableMap<String, SeasonalTenderProposal>,
    var order: MutableList<String>,
) : SeasonalTenderProposalStore {
    override fun find(code: String): SeasonalTenderProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTenderProposal) {
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

    override fun list(): MutableList<SeasonalTenderProposal> {
        val out = mutableListOf<SeasonalTenderProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTenderProposalStore(): SeasonalTenderProposalStore {
    return MemorySeasonalTenderProposalStore(
        mutableMapOf<String, SeasonalTenderProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTenderProposalStore(
    var inner: SeasonalTenderProposalStore,
    var cache: TtlCache<SeasonalTenderProposal>,
    var hits: Int,
    var misses: Int,
) : SeasonalTenderProposalStore {
    override fun find(code: String): SeasonalTenderProposal? {
        val hit = (cache.get(code) as SeasonalTenderProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTenderProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTenderProposal> = inner.list()
}

fun newCachedSeasonalTenderProposalStore(inner: SeasonalTenderProposalStore, ttl: Int, clock: Clock): SeasonalTenderProposalStore {
    return CachedSeasonalTenderProposalStore(
        inner = inner,
        cache = newTtlCache<SeasonalTenderProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tender proposals. Empty fields match all. */
data class SeasonalTenderProposalQuery(
    var unit: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTenderProposal): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTenderProposal>): MutableList<SeasonalTenderProposal> {
        val matched = filter(items) { candidate: SeasonalTenderProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTenderProposals(store: SeasonalTenderProposalStore, query: SeasonalTenderProposalQuery): MutableList<SeasonalTenderProposal> {
    return query.apply(store.list())
}
