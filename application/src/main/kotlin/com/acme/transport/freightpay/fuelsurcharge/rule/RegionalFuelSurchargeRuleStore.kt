package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel surcharge rules by code. */
interface RegionalFuelSurchargeRuleStore {
    fun find(code: String): RegionalFuelSurchargeRule?
    fun save(item: RegionalFuelSurchargeRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelSurchargeRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelSurchargeRuleStore(
    var items: MutableMap<String, RegionalFuelSurchargeRule>,
    var order: MutableList<String>,
) : RegionalFuelSurchargeRuleStore {
    override fun find(code: String): RegionalFuelSurchargeRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelSurchargeRule) {
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

    override fun list(): MutableList<RegionalFuelSurchargeRule> {
        val out = mutableListOf<RegionalFuelSurchargeRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelSurchargeRuleStore(): RegionalFuelSurchargeRuleStore {
    return MemoryRegionalFuelSurchargeRuleStore(
        mutableMapOf<String, RegionalFuelSurchargeRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelSurchargeRuleStore(
    var inner: RegionalFuelSurchargeRuleStore,
    var cache: TtlCache<RegionalFuelSurchargeRule>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelSurchargeRuleStore {
    override fun find(code: String): RegionalFuelSurchargeRule? {
        val hit = (cache.get(code) as RegionalFuelSurchargeRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelSurchargeRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelSurchargeRule> = inner.list()
}

fun newCachedRegionalFuelSurchargeRuleStore(inner: RegionalFuelSurchargeRuleStore, ttl: Int, clock: Clock): RegionalFuelSurchargeRuleStore {
    return CachedRegionalFuelSurchargeRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelSurchargeRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel surcharge rules. Empty fields match all. */
data class RegionalFuelSurchargeRuleQuery(
    var country: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelSurchargeRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RegionalFuelSurchargeRule>): MutableList<RegionalFuelSurchargeRule> {
        val matched = filter(items) { candidate: RegionalFuelSurchargeRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelSurchargeRules(store: RegionalFuelSurchargeRuleStore, query: RegionalFuelSurchargeRuleQuery): MutableList<RegionalFuelSurchargeRule> {
    return query.apply(store.list())
}
