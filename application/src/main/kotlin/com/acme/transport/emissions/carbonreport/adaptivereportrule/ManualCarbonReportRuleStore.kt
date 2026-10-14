package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual carbon report rules by code. */
interface ManualCarbonReportRuleStore {
    fun find(code: String): ManualCarbonReportRule?
    fun save(item: ManualCarbonReportRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCarbonReportRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCarbonReportRuleStore(
    var items: MutableMap<String, ManualCarbonReportRule>,
    var order: MutableList<String>,
) : ManualCarbonReportRuleStore {
    override fun find(code: String): ManualCarbonReportRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCarbonReportRule) {
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

    override fun list(): MutableList<ManualCarbonReportRule> {
        val out = mutableListOf<ManualCarbonReportRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCarbonReportRuleStore(): ManualCarbonReportRuleStore {
    return MemoryManualCarbonReportRuleStore(
        mutableMapOf<String, ManualCarbonReportRule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCarbonReportRuleStore(
    var inner: ManualCarbonReportRuleStore,
    var cache: TtlCache<ManualCarbonReportRule>,
    var hits: Int,
    var misses: Int,
) : ManualCarbonReportRuleStore {
    override fun find(code: String): ManualCarbonReportRule? {
        val hit = (cache.get(code) as ManualCarbonReportRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCarbonReportRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCarbonReportRule> = inner.list()
}

fun newCachedManualCarbonReportRuleStore(inner: ManualCarbonReportRuleStore, ttl: Int, clock: Clock): ManualCarbonReportRuleStore {
    return CachedManualCarbonReportRuleStore(
        inner = inner,
        cache = newTtlCache<ManualCarbonReportRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual carbon report rules. Empty fields match all. */
data class ManualCarbonReportRuleQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCarbonReportRule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCarbonReportRule>): MutableList<ManualCarbonReportRule> {
        val matched = filter(items) { candidate: ManualCarbonReportRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCarbonReportRules(store: ManualCarbonReportRuleStore, query: ManualCarbonReportRuleQuery): MutableList<ManualCarbonReportRule> {
    return query.apply(store.list())
}
