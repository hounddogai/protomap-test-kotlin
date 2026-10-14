package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card proposals by code. */
interface RateCardProposalStore {
    fun find(code: String): RateCardProposal?
    fun save(item: RateCardProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardProposalStore(
    var items: MutableMap<String, RateCardProposal>,
    var order: MutableList<String>,
) : RateCardProposalStore {
    override fun find(code: String): RateCardProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardProposal) {
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

    override fun list(): MutableList<RateCardProposal> {
        val out = mutableListOf<RateCardProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardProposalStore(): RateCardProposalStore {
    return MemoryRateCardProposalStore(mutableMapOf<String, RateCardProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardProposalStore(
    var inner: RateCardProposalStore,
    var cache: TtlCache<RateCardProposal>,
    var hits: Int,
    var misses: Int,
) : RateCardProposalStore {
    override fun find(code: String): RateCardProposal? {
        val hit = (cache.get(code) as RateCardProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardProposal> = inner.list()
}

fun newCachedRateCardProposalStore(inner: RateCardProposalStore, ttl: Int, clock: Clock): RateCardProposalStore {
    return CachedRateCardProposalStore(
        inner = inner,
        cache = newTtlCache<RateCardProposal>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card proposals. Empty fields match all. */
data class RateCardProposalQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardProposal): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardProposal>): MutableList<RateCardProposal> {
        val matched = filter(items) { candidate: RateCardProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardProposals(store: RateCardProposalStore, query: RateCardProposalQuery): MutableList<RateCardProposal> {
    return query.apply(store.list())
}
