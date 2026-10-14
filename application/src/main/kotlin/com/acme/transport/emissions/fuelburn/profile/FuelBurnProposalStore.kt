package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn proposals by code. */
interface FuelBurnProposalStore {
    fun find(code: String): FuelBurnProposal?
    fun save(item: FuelBurnProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnProposalStore(
    var items: MutableMap<String, FuelBurnProposal>,
    var order: MutableList<String>,
) : FuelBurnProposalStore {
    override fun find(code: String): FuelBurnProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnProposal) {
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

    override fun list(): MutableList<FuelBurnProposal> {
        val out = mutableListOf<FuelBurnProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnProposalStore(): FuelBurnProposalStore {
    return MemoryFuelBurnProposalStore(mutableMapOf<String, FuelBurnProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnProposalStore(
    var inner: FuelBurnProposalStore,
    var cache: TtlCache<FuelBurnProposal>,
    var hits: Int,
    var misses: Int,
) : FuelBurnProposalStore {
    override fun find(code: String): FuelBurnProposal? {
        val hit = (cache.get(code) as FuelBurnProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnProposal> = inner.list()
}

fun newCachedFuelBurnProposalStore(inner: FuelBurnProposalStore, ttl: Int, clock: Clock): FuelBurnProposalStore {
    return CachedFuelBurnProposalStore(
        inner = inner,
        cache = newTtlCache<FuelBurnProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn proposals. Empty fields match all. */
data class FuelBurnProposalQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FuelBurnProposal>): MutableList<FuelBurnProposal> {
        val matched = filter(items) { candidate: FuelBurnProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnProposals(store: FuelBurnProposalStore, query: FuelBurnProposalQuery): MutableList<FuelBurnProposal> {
    return query.apply(store.list())
}
