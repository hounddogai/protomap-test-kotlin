package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report overrides by code. */
interface CarbonReportOverrideStore {
    fun find(code: String): CarbonReportOverride?
    fun save(item: CarbonReportOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportOverrideStore(
    var items: MutableMap<String, CarbonReportOverride>,
    var order: MutableList<String>,
) : CarbonReportOverrideStore {
    override fun find(code: String): CarbonReportOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportOverride) {
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

    override fun list(): MutableList<CarbonReportOverride> {
        val out = mutableListOf<CarbonReportOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportOverrideStore(): CarbonReportOverrideStore {
    return MemoryCarbonReportOverrideStore(
        mutableMapOf<String, CarbonReportOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportOverrideStore(
    var inner: CarbonReportOverrideStore,
    var cache: TtlCache<CarbonReportOverride>,
    var hits: Int,
    var misses: Int,
) : CarbonReportOverrideStore {
    override fun find(code: String): CarbonReportOverride? {
        val hit = (cache.get(code) as CarbonReportOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportOverride> = inner.list()
}

fun newCachedCarbonReportOverrideStore(inner: CarbonReportOverrideStore, ttl: Int, clock: Clock): CarbonReportOverrideStore {
    return CachedCarbonReportOverrideStore(
        inner = inner,
        cache = newTtlCache<CarbonReportOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report overrides. Empty fields match all. */
data class CarbonReportOverrideQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportOverride>): MutableList<CarbonReportOverride> {
        val matched = filter(items) { candidate: CarbonReportOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportOverrides(store: CarbonReportOverrideStore, query: CarbonReportOverrideQuery): MutableList<CarbonReportOverride> {
    return query.apply(store.list())
}
