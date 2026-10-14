package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive carbon report proposals by code. */
interface AdaptiveCarbonReportProposalStore {
    fun find(code: String): AdaptiveCarbonReportProposal?
    fun save(item: AdaptiveCarbonReportProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCarbonReportProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCarbonReportProposalStore(
    var items: MutableMap<String, AdaptiveCarbonReportProposal>,
    var order: MutableList<String>,
) : AdaptiveCarbonReportProposalStore {
    override fun find(code: String): AdaptiveCarbonReportProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCarbonReportProposal) {
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

    override fun list(): MutableList<AdaptiveCarbonReportProposal> {
        val out = mutableListOf<AdaptiveCarbonReportProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCarbonReportProposalStore(): AdaptiveCarbonReportProposalStore {
    return MemoryAdaptiveCarbonReportProposalStore(
        mutableMapOf<String, AdaptiveCarbonReportProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCarbonReportProposalStore(
    var inner: AdaptiveCarbonReportProposalStore,
    var cache: TtlCache<AdaptiveCarbonReportProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCarbonReportProposalStore {
    override fun find(code: String): AdaptiveCarbonReportProposal? {
        val hit = (cache.get(code) as AdaptiveCarbonReportProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCarbonReportProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCarbonReportProposal> = inner.list()
}

fun newCachedAdaptiveCarbonReportProposalStore(inner: AdaptiveCarbonReportProposalStore, ttl: Int, clock: Clock): AdaptiveCarbonReportProposalStore {
    return CachedAdaptiveCarbonReportProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCarbonReportProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive carbon report proposals. Empty fields match all. */
data class AdaptiveCarbonReportProposalQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCarbonReportProposal): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCarbonReportProposal>): MutableList<AdaptiveCarbonReportProposal> {
        val matched = filter(items) { candidate: AdaptiveCarbonReportProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCarbonReportProposals(store: AdaptiveCarbonReportProposalStore, query: AdaptiveCarbonReportProposalQuery): MutableList<AdaptiveCarbonReportProposal> {
    return query.apply(store.list())
}
