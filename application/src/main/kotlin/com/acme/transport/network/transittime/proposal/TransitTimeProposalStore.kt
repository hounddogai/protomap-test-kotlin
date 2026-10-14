package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time proposals by code. */
interface TransitTimeProposalStore {
    fun find(code: String): TransitTimeProposal?
    fun save(item: TransitTimeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeProposalStore(
    var items: MutableMap<String, TransitTimeProposal>,
    var order: MutableList<String>,
) : TransitTimeProposalStore {
    override fun find(code: String): TransitTimeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeProposal) {
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

    override fun list(): MutableList<TransitTimeProposal> {
        val out = mutableListOf<TransitTimeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeProposalStore(): TransitTimeProposalStore {
    return MemoryTransitTimeProposalStore(mutableMapOf<String, TransitTimeProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeProposalStore(
    var inner: TransitTimeProposalStore,
    var cache: TtlCache<TransitTimeProposal>,
    var hits: Int,
    var misses: Int,
) : TransitTimeProposalStore {
    override fun find(code: String): TransitTimeProposal? {
        val hit = (cache.get(code) as TransitTimeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeProposal> = inner.list()
}

fun newCachedTransitTimeProposalStore(inner: TransitTimeProposalStore, ttl: Int, clock: Clock): TransitTimeProposalStore {
    return CachedTransitTimeProposalStore(
        inner = inner,
        cache = newTtlCache<TransitTimeProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time proposals. Empty fields match all. */
data class TransitTimeProposalQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeProposal>): MutableList<TransitTimeProposal> {
        val matched = filter(items) { candidate: TransitTimeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeProposals(store: TransitTimeProposalStore, query: TransitTimeProposalQuery): MutableList<TransitTimeProposal> {
    return query.apply(store.list())
}
