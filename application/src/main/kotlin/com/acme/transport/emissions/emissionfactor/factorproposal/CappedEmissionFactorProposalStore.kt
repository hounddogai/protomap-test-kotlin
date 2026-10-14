package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped emission factor proposals by code. */
interface CappedEmissionFactorProposalStore {
    fun find(code: String): CappedEmissionFactorProposal?
    fun save(item: CappedEmissionFactorProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedEmissionFactorProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedEmissionFactorProposalStore(
    var items: MutableMap<String, CappedEmissionFactorProposal>,
    var order: MutableList<String>,
) : CappedEmissionFactorProposalStore {
    override fun find(code: String): CappedEmissionFactorProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedEmissionFactorProposal) {
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

    override fun list(): MutableList<CappedEmissionFactorProposal> {
        val out = mutableListOf<CappedEmissionFactorProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedEmissionFactorProposalStore(): CappedEmissionFactorProposalStore {
    return MemoryCappedEmissionFactorProposalStore(
        mutableMapOf<String, CappedEmissionFactorProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedEmissionFactorProposalStore(
    var inner: CappedEmissionFactorProposalStore,
    var cache: TtlCache<CappedEmissionFactorProposal>,
    var hits: Int,
    var misses: Int,
) : CappedEmissionFactorProposalStore {
    override fun find(code: String): CappedEmissionFactorProposal? {
        val hit = (cache.get(code) as CappedEmissionFactorProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedEmissionFactorProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedEmissionFactorProposal> = inner.list()
}

fun newCachedCappedEmissionFactorProposalStore(inner: CappedEmissionFactorProposalStore, ttl: Int, clock: Clock): CappedEmissionFactorProposalStore {
    return CachedCappedEmissionFactorProposalStore(
        inner = inner,
        cache = newTtlCache<CappedEmissionFactorProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped emission factor proposals. Empty fields match all. */
data class CappedEmissionFactorProposalQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedEmissionFactorProposal): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedEmissionFactorProposal>): MutableList<CappedEmissionFactorProposal> {
        val matched = filter(items) { candidate: CappedEmissionFactorProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedEmissionFactorProposals(store: CappedEmissionFactorProposalStore, query: CappedEmissionFactorProposalQuery): MutableList<CappedEmissionFactorProposal> {
    return query.apply(store.list())
}
