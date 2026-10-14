package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn rules by code. */
interface RegionalFuelBurnRuleStore {
    fun find(code: String): RegionalFuelBurnRule?
    fun save(item: RegionalFuelBurnRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnRuleStore(
    var items: MutableMap<String, RegionalFuelBurnRule>,
    var order: MutableList<String>,
) : RegionalFuelBurnRuleStore {
    override fun find(code: String): RegionalFuelBurnRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnRule) {
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

    override fun list(): MutableList<RegionalFuelBurnRule> {
        val out = mutableListOf<RegionalFuelBurnRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnRuleStore(): RegionalFuelBurnRuleStore {
    return MemoryRegionalFuelBurnRuleStore(
        mutableMapOf<String, RegionalFuelBurnRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnRuleStore(
    var inner: RegionalFuelBurnRuleStore,
    var cache: TtlCache<RegionalFuelBurnRule>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnRuleStore {
    override fun find(code: String): RegionalFuelBurnRule? {
        val hit = (cache.get(code) as RegionalFuelBurnRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnRule> = inner.list()
}

fun newCachedRegionalFuelBurnRuleStore(inner: RegionalFuelBurnRuleStore, ttl: Int, clock: Clock): RegionalFuelBurnRuleStore {
    return CachedRegionalFuelBurnRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn rules. Empty fields match all. */
data class RegionalFuelBurnRuleQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFuelBurnRule>): MutableList<RegionalFuelBurnRule> {
        val matched = filter(items) { candidate: RegionalFuelBurnRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnRules(store: RegionalFuelBurnRuleStore, query: RegionalFuelBurnRuleQuery): MutableList<RegionalFuelBurnRule> {
    return query.apply(store.list())
}
