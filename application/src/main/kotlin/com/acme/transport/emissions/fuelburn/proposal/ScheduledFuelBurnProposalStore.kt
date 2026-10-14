package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel burn proposals by code. */
interface ScheduledFuelBurnProposalStore {
    fun find(code: String): ScheduledFuelBurnProposal?
    fun save(item: ScheduledFuelBurnProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelBurnProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelBurnProposalStore(
    var items: MutableMap<String, ScheduledFuelBurnProposal>,
    var order: MutableList<String>,
) : ScheduledFuelBurnProposalStore {
    override fun find(code: String): ScheduledFuelBurnProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelBurnProposal) {
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

    override fun list(): MutableList<ScheduledFuelBurnProposal> {
        val out = mutableListOf<ScheduledFuelBurnProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelBurnProposalStore(): ScheduledFuelBurnProposalStore {
    return MemoryScheduledFuelBurnProposalStore(
        mutableMapOf<String, ScheduledFuelBurnProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelBurnProposalStore(
    var inner: ScheduledFuelBurnProposalStore,
    var cache: TtlCache<ScheduledFuelBurnProposal>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelBurnProposalStore {
    override fun find(code: String): ScheduledFuelBurnProposal? {
        val hit = (cache.get(code) as ScheduledFuelBurnProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelBurnProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelBurnProposal> = inner.list()
}

fun newCachedScheduledFuelBurnProposalStore(inner: ScheduledFuelBurnProposalStore, ttl: Int, clock: Clock): ScheduledFuelBurnProposalStore {
    return CachedScheduledFuelBurnProposalStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelBurnProposal>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel burn proposals. Empty fields match all. */
data class ScheduledFuelBurnProposalQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelBurnProposal): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelBurnProposal>): MutableList<ScheduledFuelBurnProposal> {
        val matched = filter(items) { candidate: ScheduledFuelBurnProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelBurnProposals(store: ScheduledFuelBurnProposalStore, query: ScheduledFuelBurnProposalQuery): MutableList<ScheduledFuelBurnProposal> {
    return query.apply(store.list())
}
