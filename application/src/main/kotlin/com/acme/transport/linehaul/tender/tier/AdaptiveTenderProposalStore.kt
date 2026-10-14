package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive tender proposals by code. */
interface AdaptiveTenderProposalStore {
    fun find(code: String): AdaptiveTenderProposal?
    fun save(item: AdaptiveTenderProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTenderProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTenderProposalStore(
    var items: MutableMap<String, AdaptiveTenderProposal>,
    var order: MutableList<String>,
) : AdaptiveTenderProposalStore {
    override fun find(code: String): AdaptiveTenderProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTenderProposal) {
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

    override fun list(): MutableList<AdaptiveTenderProposal> {
        val out = mutableListOf<AdaptiveTenderProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTenderProposalStore(): AdaptiveTenderProposalStore {
    return MemoryAdaptiveTenderProposalStore(
        mutableMapOf<String, AdaptiveTenderProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTenderProposalStore(
    var inner: AdaptiveTenderProposalStore,
    var cache: TtlCache<AdaptiveTenderProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTenderProposalStore {
    override fun find(code: String): AdaptiveTenderProposal? {
        val hit = (cache.get(code) as AdaptiveTenderProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTenderProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTenderProposal> = inner.list()
}

fun newCachedAdaptiveTenderProposalStore(inner: AdaptiveTenderProposalStore, ttl: Int, clock: Clock): AdaptiveTenderProposalStore {
    return CachedAdaptiveTenderProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTenderProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tender proposals. Empty fields match all. */
data class AdaptiveTenderProposalQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTenderProposal): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTenderProposal>): MutableList<AdaptiveTenderProposal> {
        val matched = filter(items) { candidate: AdaptiveTenderProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTenderProposals(store: AdaptiveTenderProposalStore, query: AdaptiveTenderProposalQuery): MutableList<AdaptiveTenderProposal> {
    return query.apply(store.list())
}
