package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional tire proposals by code. */
interface RegionalTireProposalStore {
    fun find(code: String): RegionalTireProposal?
    fun save(item: RegionalTireProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTireProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTireProposalStore(
    var items: MutableMap<String, RegionalTireProposal>,
    var order: MutableList<String>,
) : RegionalTireProposalStore {
    override fun find(code: String): RegionalTireProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTireProposal) {
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

    override fun list(): MutableList<RegionalTireProposal> {
        val out = mutableListOf<RegionalTireProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTireProposalStore(): RegionalTireProposalStore {
    return MemoryRegionalTireProposalStore(
        mutableMapOf<String, RegionalTireProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTireProposalStore(
    var inner: RegionalTireProposalStore,
    var cache: TtlCache<RegionalTireProposal>,
    var hits: Int,
    var misses: Int,
) : RegionalTireProposalStore {
    override fun find(code: String): RegionalTireProposal? {
        val hit = (cache.get(code) as RegionalTireProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTireProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTireProposal> = inner.list()
}

fun newCachedRegionalTireProposalStore(inner: RegionalTireProposalStore, ttl: Int, clock: Clock): RegionalTireProposalStore {
    return CachedRegionalTireProposalStore(
        inner = inner,
        cache = newTtlCache<RegionalTireProposal>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional tire proposals. Empty fields match all. */
data class RegionalTireProposalQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTireProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTireProposal>): MutableList<RegionalTireProposal> {
        val matched = filter(items) { candidate: RegionalTireProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTireProposals(store: RegionalTireProposalStore, query: RegionalTireProposalQuery): MutableList<RegionalTireProposal> {
    return query.apply(store.list())
}
