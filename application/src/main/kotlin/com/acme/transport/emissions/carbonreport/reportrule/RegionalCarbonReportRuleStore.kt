package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report rules by code. */
interface RegionalCarbonReportRuleStore {
    fun find(code: String): RegionalCarbonReportRule?
    fun save(item: RegionalCarbonReportRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportRuleStore(
    var items: MutableMap<String, RegionalCarbonReportRule>,
    var order: MutableList<String>,
) : RegionalCarbonReportRuleStore {
    override fun find(code: String): RegionalCarbonReportRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportRule) {
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

    override fun list(): MutableList<RegionalCarbonReportRule> {
        val out = mutableListOf<RegionalCarbonReportRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportRuleStore(): RegionalCarbonReportRuleStore {
    return MemoryRegionalCarbonReportRuleStore(
        mutableMapOf<String, RegionalCarbonReportRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportRuleStore(
    var inner: RegionalCarbonReportRuleStore,
    var cache: TtlCache<RegionalCarbonReportRule>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportRuleStore {
    override fun find(code: String): RegionalCarbonReportRule? {
        val hit = (cache.get(code) as RegionalCarbonReportRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportRule> = inner.list()
}

fun newCachedRegionalCarbonReportRuleStore(inner: RegionalCarbonReportRuleStore, ttl: Int, clock: Clock): RegionalCarbonReportRuleStore {
    return CachedRegionalCarbonReportRuleStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report rules. Empty fields match all. */
data class RegionalCarbonReportRuleQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportRule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportRule>): MutableList<RegionalCarbonReportRule> {
        val matched = filter(items) { candidate: RegionalCarbonReportRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportRules(store: RegionalCarbonReportRuleStore, query: RegionalCarbonReportRuleQuery): MutableList<RegionalCarbonReportRule> {
    return query.apply(store.list())
}
