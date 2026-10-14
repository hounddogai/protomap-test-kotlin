package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted broker plans by code. */
interface WeightedBrokerPlanStore {
    fun find(code: String): WeightedBrokerPlan?
    fun save(item: WeightedBrokerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBrokerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBrokerPlanStore(
    var items: MutableMap<String, WeightedBrokerPlan>,
    var order: MutableList<String>,
) : WeightedBrokerPlanStore {
    override fun find(code: String): WeightedBrokerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBrokerPlan) {
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

    override fun list(): MutableList<WeightedBrokerPlan> {
        val out = mutableListOf<WeightedBrokerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBrokerPlanStore(): WeightedBrokerPlanStore {
    return MemoryWeightedBrokerPlanStore(mutableMapOf<String, WeightedBrokerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBrokerPlanStore(
    var inner: WeightedBrokerPlanStore,
    var cache: TtlCache<WeightedBrokerPlan>,
    var hits: Int,
    var misses: Int,
) : WeightedBrokerPlanStore {
    override fun find(code: String): WeightedBrokerPlan? {
        val hit = (cache.get(code) as WeightedBrokerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBrokerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBrokerPlan> = inner.list()
}

fun newCachedWeightedBrokerPlanStore(inner: WeightedBrokerPlanStore, ttl: Int, clock: Clock): WeightedBrokerPlanStore {
    return CachedWeightedBrokerPlanStore(
        inner = inner,
        cache = newTtlCache<WeightedBrokerPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted broker plans. Empty fields match all. */
data class WeightedBrokerPlanQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBrokerPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedBrokerPlan>): MutableList<WeightedBrokerPlan> {
        val matched = filter(items) { candidate: WeightedBrokerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBrokerPlans(store: WeightedBrokerPlanStore, query: WeightedBrokerPlanQuery): MutableList<WeightedBrokerPlan> {
    return query.apply(store.list())
}
