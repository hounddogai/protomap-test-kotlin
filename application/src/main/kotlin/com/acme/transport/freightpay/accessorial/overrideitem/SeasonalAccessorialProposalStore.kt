package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal accessorial proposals by code. */
interface SeasonalAccessorialProposalStore {
    fun find(code: String): SeasonalAccessorialProposal?
    fun save(item: SeasonalAccessorialProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAccessorialProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAccessorialProposalStore(
    var items: MutableMap<String, SeasonalAccessorialProposal>,
    var order: MutableList<String>,
) : SeasonalAccessorialProposalStore {
    override fun find(code: String): SeasonalAccessorialProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAccessorialProposal) {
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

    override fun list(): MutableList<SeasonalAccessorialProposal> {
        val out = mutableListOf<SeasonalAccessorialProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAccessorialProposalStore(): SeasonalAccessorialProposalStore {
    return MemorySeasonalAccessorialProposalStore(
        mutableMapOf<String, SeasonalAccessorialProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAccessorialProposalStore(
    var inner: SeasonalAccessorialProposalStore,
    var cache: TtlCache<SeasonalAccessorialProposal>,
    var hits: Int,
    var misses: Int,
) : SeasonalAccessorialProposalStore {
    override fun find(code: String): SeasonalAccessorialProposal? {
        val hit = (cache.get(code) as SeasonalAccessorialProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAccessorialProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAccessorialProposal> = inner.list()
}

fun newCachedSeasonalAccessorialProposalStore(inner: SeasonalAccessorialProposalStore, ttl: Int, clock: Clock): SeasonalAccessorialProposalStore {
    return CachedSeasonalAccessorialProposalStore(
        inner = inner,
        cache = newTtlCache<SeasonalAccessorialProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal accessorial proposals. Empty fields match all. */
data class SeasonalAccessorialProposalQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAccessorialProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAccessorialProposal>): MutableList<SeasonalAccessorialProposal> {
        val matched = filter(items) { candidate: SeasonalAccessorialProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAccessorialProposals(store: SeasonalAccessorialProposalStore, query: SeasonalAccessorialProposalQuery): MutableList<SeasonalAccessorialProposal> {
    return query.apply(store.list())
}
