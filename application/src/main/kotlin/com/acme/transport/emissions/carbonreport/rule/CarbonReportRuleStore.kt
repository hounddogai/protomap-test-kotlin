package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report rules by code. */
interface CarbonReportRuleStore {
    fun find(code: String): CarbonReportRule?
    fun save(item: CarbonReportRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportRuleStore(
    var items: MutableMap<String, CarbonReportRule>,
    var order: MutableList<String>,
) : CarbonReportRuleStore {
    override fun find(code: String): CarbonReportRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportRule) {
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

    override fun list(): MutableList<CarbonReportRule> {
        val out = mutableListOf<CarbonReportRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportRuleStore(): CarbonReportRuleStore {
    return MemoryCarbonReportRuleStore(mutableMapOf<String, CarbonReportRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportRuleStore(
    var inner: CarbonReportRuleStore,
    var cache: TtlCache<CarbonReportRule>,
    var hits: Int,
    var misses: Int,
) : CarbonReportRuleStore {
    override fun find(code: String): CarbonReportRule? {
        val hit = (cache.get(code) as CarbonReportRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportRule> = inner.list()
}

fun newCachedCarbonReportRuleStore(inner: CarbonReportRuleStore, ttl: Int, clock: Clock): CarbonReportRuleStore {
    return CachedCarbonReportRuleStore(
        inner = inner,
        cache = newTtlCache<CarbonReportRule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report rules. Empty fields match all. */
data class CarbonReportRuleQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportRule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportRule>): MutableList<CarbonReportRule> {
        val matched = filter(items) { candidate: CarbonReportRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportRules(store: CarbonReportRuleStore, query: CarbonReportRuleQuery): MutableList<CarbonReportRule> {
    return query.apply(store.list())
}
