package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted tariff policies by code. */
interface WeightedTariffPolicyStore {
    fun find(code: String): WeightedTariffPolicy?
    fun save(item: WeightedTariffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTariffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTariffPolicyStore(
    var items: MutableMap<String, WeightedTariffPolicy>,
    var order: MutableList<String>,
) : WeightedTariffPolicyStore {
    override fun find(code: String): WeightedTariffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTariffPolicy) {
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

    override fun list(): MutableList<WeightedTariffPolicy> {
        val out = mutableListOf<WeightedTariffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTariffPolicyStore(): WeightedTariffPolicyStore {
    return MemoryWeightedTariffPolicyStore(
        mutableMapOf<String, WeightedTariffPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTariffPolicyStore(
    var inner: WeightedTariffPolicyStore,
    var cache: TtlCache<WeightedTariffPolicy>,
    var hits: Int,
    var misses: Int,
) : WeightedTariffPolicyStore {
    override fun find(code: String): WeightedTariffPolicy? {
        val hit = (cache.get(code) as WeightedTariffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTariffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTariffPolicy> = inner.list()
}

fun newCachedWeightedTariffPolicyStore(inner: WeightedTariffPolicyStore, ttl: Int, clock: Clock): WeightedTariffPolicyStore {
    return CachedWeightedTariffPolicyStore(
        inner = inner,
        cache = newTtlCache<WeightedTariffPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tariff policies. Empty fields match all. */
data class WeightedTariffPolicyQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTariffPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTariffPolicy>): MutableList<WeightedTariffPolicy> {
        val matched = filter(items) { candidate: WeightedTariffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTariffPolicies(store: WeightedTariffPolicyStore, query: WeightedTariffPolicyQuery): MutableList<WeightedTariffPolicy> {
    return query.apply(store.list())
}
