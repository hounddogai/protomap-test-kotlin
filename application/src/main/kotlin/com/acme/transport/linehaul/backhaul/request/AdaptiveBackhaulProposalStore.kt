package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul proposals by code. */
interface AdaptiveBackhaulProposalStore {
    fun find(code: String): AdaptiveBackhaulProposal?
    fun save(item: AdaptiveBackhaulProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulProposalStore(
    var items: MutableMap<String, AdaptiveBackhaulProposal>,
    var order: MutableList<String>,
) : AdaptiveBackhaulProposalStore {
    override fun find(code: String): AdaptiveBackhaulProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulProposal) {
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

    override fun list(): MutableList<AdaptiveBackhaulProposal> {
        val out = mutableListOf<AdaptiveBackhaulProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulProposalStore(): AdaptiveBackhaulProposalStore {
    return MemoryAdaptiveBackhaulProposalStore(
        mutableMapOf<String, AdaptiveBackhaulProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulProposalStore(
    var inner: AdaptiveBackhaulProposalStore,
    var cache: TtlCache<AdaptiveBackhaulProposal>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulProposalStore {
    override fun find(code: String): AdaptiveBackhaulProposal? {
        val hit = (cache.get(code) as AdaptiveBackhaulProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulProposal> = inner.list()
}

fun newCachedAdaptiveBackhaulProposalStore(inner: AdaptiveBackhaulProposalStore, ttl: Int, clock: Clock): AdaptiveBackhaulProposalStore {
    return CachedAdaptiveBackhaulProposalStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul proposals. Empty fields match all. */
data class AdaptiveBackhaulProposalQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBackhaulProposal>): MutableList<AdaptiveBackhaulProposal> {
        val matched = filter(items) { candidate: AdaptiveBackhaulProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulProposals(store: AdaptiveBackhaulProposalStore, query: AdaptiveBackhaulProposalQuery): MutableList<AdaptiveBackhaulProposal> {
    return query.apply(store.list())
}
