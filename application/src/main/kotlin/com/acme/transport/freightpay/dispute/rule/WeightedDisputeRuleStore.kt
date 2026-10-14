package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted dispute rules by code. */
interface WeightedDisputeRuleStore {
    fun find(code: String): WeightedDisputeRule?
    fun save(item: WeightedDisputeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDisputeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDisputeRuleStore(
    var items: MutableMap<String, WeightedDisputeRule>,
    var order: MutableList<String>,
) : WeightedDisputeRuleStore {
    override fun find(code: String): WeightedDisputeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDisputeRule) {
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

    override fun list(): MutableList<WeightedDisputeRule> {
        val out = mutableListOf<WeightedDisputeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDisputeRuleStore(): WeightedDisputeRuleStore {
    return MemoryWeightedDisputeRuleStore(mutableMapOf<String, WeightedDisputeRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDisputeRuleStore(
    var inner: WeightedDisputeRuleStore,
    var cache: TtlCache<WeightedDisputeRule>,
    var hits: Int,
    var misses: Int,
) : WeightedDisputeRuleStore {
    override fun find(code: String): WeightedDisputeRule? {
        val hit = (cache.get(code) as WeightedDisputeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDisputeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDisputeRule> = inner.list()
}

fun newCachedWeightedDisputeRuleStore(inner: WeightedDisputeRuleStore, ttl: Int, clock: Clock): WeightedDisputeRuleStore {
    return CachedWeightedDisputeRuleStore(
        inner = inner,
        cache = newTtlCache<WeightedDisputeRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dispute rules. Empty fields match all. */
data class WeightedDisputeRuleQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDisputeRule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDisputeRule>): MutableList<WeightedDisputeRule> {
        val matched = filter(items) { candidate: WeightedDisputeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDisputeRules(store: WeightedDisputeRuleStore, query: WeightedDisputeRuleQuery): MutableList<WeightedDisputeRule> {
    return query.apply(store.list())
}
