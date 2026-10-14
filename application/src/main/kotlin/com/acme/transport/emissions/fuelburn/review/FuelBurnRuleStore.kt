package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn rules by code. */
interface FuelBurnRuleStore {
    fun find(code: String): FuelBurnRule?
    fun save(item: FuelBurnRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnRuleStore(
    var items: MutableMap<String, FuelBurnRule>,
    var order: MutableList<String>,
) : FuelBurnRuleStore {
    override fun find(code: String): FuelBurnRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnRule) {
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

    override fun list(): MutableList<FuelBurnRule> {
        val out = mutableListOf<FuelBurnRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnRuleStore(): FuelBurnRuleStore {
    return MemoryFuelBurnRuleStore(mutableMapOf<String, FuelBurnRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnRuleStore(
    var inner: FuelBurnRuleStore,
    var cache: TtlCache<FuelBurnRule>,
    var hits: Int,
    var misses: Int,
) : FuelBurnRuleStore {
    override fun find(code: String): FuelBurnRule? {
        val hit = (cache.get(code) as FuelBurnRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnRule> = inner.list()
}

fun newCachedFuelBurnRuleStore(inner: FuelBurnRuleStore, ttl: Int, clock: Clock): FuelBurnRuleStore {
    return CachedFuelBurnRuleStore(
        inner = inner,
        cache = newTtlCache<FuelBurnRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn rules. Empty fields match all. */
data class FuelBurnRuleQuery(
    var locale: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnRule>): MutableList<FuelBurnRule> {
        val matched = filter(items) { candidate: FuelBurnRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnRules(store: FuelBurnRuleStore, query: FuelBurnRuleQuery): MutableList<FuelBurnRule> {
    return query.apply(store.list())
}
