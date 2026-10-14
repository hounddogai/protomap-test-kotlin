package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted utilization rules by code. */
interface WeightedUtilizationRuleStore {
    fun find(code: String): WeightedUtilizationRule?
    fun save(item: WeightedUtilizationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedUtilizationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedUtilizationRuleStore(
    var items: MutableMap<String, WeightedUtilizationRule>,
    var order: MutableList<String>,
) : WeightedUtilizationRuleStore {
    override fun find(code: String): WeightedUtilizationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedUtilizationRule) {
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

    override fun list(): MutableList<WeightedUtilizationRule> {
        val out = mutableListOf<WeightedUtilizationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedUtilizationRuleStore(): WeightedUtilizationRuleStore {
    return MemoryWeightedUtilizationRuleStore(
        mutableMapOf<String, WeightedUtilizationRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedUtilizationRuleStore(
    var inner: WeightedUtilizationRuleStore,
    var cache: TtlCache<WeightedUtilizationRule>,
    var hits: Int,
    var misses: Int,
) : WeightedUtilizationRuleStore {
    override fun find(code: String): WeightedUtilizationRule? {
        val hit = (cache.get(code) as WeightedUtilizationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedUtilizationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedUtilizationRule> = inner.list()
}

fun newCachedWeightedUtilizationRuleStore(inner: WeightedUtilizationRuleStore, ttl: Int, clock: Clock): WeightedUtilizationRuleStore {
    return CachedWeightedUtilizationRuleStore(
        inner = inner,
        cache = newTtlCache<WeightedUtilizationRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted utilization rules. Empty fields match all. */
data class WeightedUtilizationRuleQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedUtilizationRule): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedUtilizationRule>): MutableList<WeightedUtilizationRule> {
        val matched = filter(items) { candidate: WeightedUtilizationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedUtilizationRules(store: WeightedUtilizationRuleStore, query: WeightedUtilizationRuleQuery): MutableList<WeightedUtilizationRule> {
    return query.apply(store.list())
}
