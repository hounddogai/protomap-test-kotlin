package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel surcharge proposals by code. */
interface WeightedFuelSurchargeProposalStore {
    fun find(code: String): WeightedFuelSurchargeProposal?
    fun save(item: WeightedFuelSurchargeProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelSurchargeProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelSurchargeProposalStore(
    var items: MutableMap<String, WeightedFuelSurchargeProposal>,
    var order: MutableList<String>,
) : WeightedFuelSurchargeProposalStore {
    override fun find(code: String): WeightedFuelSurchargeProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelSurchargeProposal) {
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

    override fun list(): MutableList<WeightedFuelSurchargeProposal> {
        val out = mutableListOf<WeightedFuelSurchargeProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelSurchargeProposalStore(): WeightedFuelSurchargeProposalStore {
    return MemoryWeightedFuelSurchargeProposalStore(
        mutableMapOf<String, WeightedFuelSurchargeProposal>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelSurchargeProposalStore(
    var inner: WeightedFuelSurchargeProposalStore,
    var cache: TtlCache<WeightedFuelSurchargeProposal>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelSurchargeProposalStore {
    override fun find(code: String): WeightedFuelSurchargeProposal? {
        val hit = (cache.get(code) as WeightedFuelSurchargeProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelSurchargeProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelSurchargeProposal> = inner.list()
}

fun newCachedWeightedFuelSurchargeProposalStore(inner: WeightedFuelSurchargeProposalStore, ttl: Int, clock: Clock): WeightedFuelSurchargeProposalStore {
    return CachedWeightedFuelSurchargeProposalStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelSurchargeProposal>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel surcharge proposals. Empty fields match all. */
data class WeightedFuelSurchargeProposalQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelSurchargeProposal): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedFuelSurchargeProposal>): MutableList<WeightedFuelSurchargeProposal> {
        val matched = filter(items) { candidate: WeightedFuelSurchargeProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelSurchargeProposals(store: WeightedFuelSurchargeProposalStore, query: WeightedFuelSurchargeProposalQuery): MutableList<WeightedFuelSurchargeProposal> {
    return query.apply(store.list())
}
