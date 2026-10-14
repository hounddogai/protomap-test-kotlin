package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor proposals by code. */
interface EmissionFactorProposalStore {
    fun find(code: String): EmissionFactorProposal?
    fun save(item: EmissionFactorProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorProposalStore(
    var items: MutableMap<String, EmissionFactorProposal>,
    var order: MutableList<String>,
) : EmissionFactorProposalStore {
    override fun find(code: String): EmissionFactorProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorProposal) {
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

    override fun list(): MutableList<EmissionFactorProposal> {
        val out = mutableListOf<EmissionFactorProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorProposalStore(): EmissionFactorProposalStore {
    return MemoryEmissionFactorProposalStore(
        mutableMapOf<String, EmissionFactorProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorProposalStore(
    var inner: EmissionFactorProposalStore,
    var cache: TtlCache<EmissionFactorProposal>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorProposalStore {
    override fun find(code: String): EmissionFactorProposal? {
        val hit = (cache.get(code) as EmissionFactorProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorProposal> = inner.list()
}

fun newCachedEmissionFactorProposalStore(inner: EmissionFactorProposalStore, ttl: Int, clock: Clock): EmissionFactorProposalStore {
    return CachedEmissionFactorProposalStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor proposals. Empty fields match all. */
data class EmissionFactorProposalQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorProposal): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<EmissionFactorProposal>): MutableList<EmissionFactorProposal> {
        val matched = filter(items) { candidate: EmissionFactorProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorProposals(store: EmissionFactorProposalStore, query: EmissionFactorProposalQuery): MutableList<EmissionFactorProposal> {
    return query.apply(store.list())
}
