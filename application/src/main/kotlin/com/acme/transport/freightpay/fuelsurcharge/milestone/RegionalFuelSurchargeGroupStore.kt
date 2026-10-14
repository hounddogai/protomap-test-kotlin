package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel surcharge groups by code. */
interface RegionalFuelSurchargeGroupStore {
    fun find(code: String): RegionalFuelSurchargeGroup?
    fun save(item: RegionalFuelSurchargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelSurchargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelSurchargeGroupStore(
    var items: MutableMap<String, RegionalFuelSurchargeGroup>,
    var order: MutableList<String>,
) : RegionalFuelSurchargeGroupStore {
    override fun find(code: String): RegionalFuelSurchargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelSurchargeGroup) {
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

    override fun list(): MutableList<RegionalFuelSurchargeGroup> {
        val out = mutableListOf<RegionalFuelSurchargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelSurchargeGroupStore(): RegionalFuelSurchargeGroupStore {
    return MemoryRegionalFuelSurchargeGroupStore(
        mutableMapOf<String, RegionalFuelSurchargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelSurchargeGroupStore(
    var inner: RegionalFuelSurchargeGroupStore,
    var cache: TtlCache<RegionalFuelSurchargeGroup>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelSurchargeGroupStore {
    override fun find(code: String): RegionalFuelSurchargeGroup? {
        val hit = (cache.get(code) as RegionalFuelSurchargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelSurchargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelSurchargeGroup> = inner.list()
}

fun newCachedRegionalFuelSurchargeGroupStore(inner: RegionalFuelSurchargeGroupStore, ttl: Int, clock: Clock): RegionalFuelSurchargeGroupStore {
    return CachedRegionalFuelSurchargeGroupStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelSurchargeGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel surcharge groups. Empty fields match all. */
data class RegionalFuelSurchargeGroupQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelSurchargeGroup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFuelSurchargeGroup>): MutableList<RegionalFuelSurchargeGroup> {
        val matched = filter(items) { candidate: RegionalFuelSurchargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelSurchargeGroups(store: RegionalFuelSurchargeGroupStore, query: RegionalFuelSurchargeGroupQuery): MutableList<RegionalFuelSurchargeGroup> {
    return query.apply(store.list())
}
