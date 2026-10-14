package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tariff rules by code. */
interface SeasonalTariffRuleStore {
    fun find(code: String): SeasonalTariffRule?
    fun save(item: SeasonalTariffRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTariffRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTariffRuleStore(
    var items: MutableMap<String, SeasonalTariffRule>,
    var order: MutableList<String>,
) : SeasonalTariffRuleStore {
    override fun find(code: String): SeasonalTariffRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTariffRule) {
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

    override fun list(): MutableList<SeasonalTariffRule> {
        val out = mutableListOf<SeasonalTariffRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTariffRuleStore(): SeasonalTariffRuleStore {
    return MemorySeasonalTariffRuleStore(mutableMapOf<String, SeasonalTariffRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTariffRuleStore(
    var inner: SeasonalTariffRuleStore,
    var cache: TtlCache<SeasonalTariffRule>,
    var hits: Int,
    var misses: Int,
) : SeasonalTariffRuleStore {
    override fun find(code: String): SeasonalTariffRule? {
        val hit = (cache.get(code) as SeasonalTariffRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTariffRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTariffRule> = inner.list()
}

fun newCachedSeasonalTariffRuleStore(inner: SeasonalTariffRuleStore, ttl: Int, clock: Clock): SeasonalTariffRuleStore {
    return CachedSeasonalTariffRuleStore(
        inner = inner,
        cache = newTtlCache<SeasonalTariffRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tariff rules. Empty fields match all. */
data class SeasonalTariffRuleQuery(
    var category: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTariffRule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTariffRule>): MutableList<SeasonalTariffRule> {
        val matched = filter(items) { candidate: SeasonalTariffRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTariffRules(store: SeasonalTariffRuleStore, query: SeasonalTariffRuleQuery): MutableList<SeasonalTariffRule> {
    return query.apply(store.list())
}
