package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge proposals by code. */
interface FuelSurchargeProposalStore {
    fun find(code: String): FuelSurchargeProposal?
    fun save(item: FuelSurchargeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeProposalStore(
    var items: MutableMap<String, FuelSurchargeProposal>,
    var order: MutableList<String>,
) : FuelSurchargeProposalStore {
    override fun find(code: String): FuelSurchargeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeProposal) {
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

    override fun list(): MutableList<FuelSurchargeProposal> {
        val out = mutableListOf<FuelSurchargeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeProposalStore(): FuelSurchargeProposalStore {
    return MemoryFuelSurchargeProposalStore(
        mutableMapOf<String, FuelSurchargeProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeProposalStore(
    var inner: FuelSurchargeProposalStore,
    var cache: TtlCache<FuelSurchargeProposal>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeProposalStore {
    override fun find(code: String): FuelSurchargeProposal? {
        val hit = (cache.get(code) as FuelSurchargeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeProposal> = inner.list()
}

fun newCachedFuelSurchargeProposalStore(inner: FuelSurchargeProposalStore, ttl: Int, clock: Clock): FuelSurchargeProposalStore {
    return CachedFuelSurchargeProposalStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge proposals. Empty fields match all. */
data class FuelSurchargeProposalQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeProposal>): MutableList<FuelSurchargeProposal> {
        val matched = filter(items) { candidate: FuelSurchargeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeProposals(store: FuelSurchargeProposalStore, query: FuelSurchargeProposalQuery): MutableList<FuelSurchargeProposal> {
    return query.apply(store.list())
}
