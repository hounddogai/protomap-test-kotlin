package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report groups by code. */
interface RegionalCarbonReportGroupStore {
    fun find(code: String): RegionalCarbonReportGroup?
    fun save(item: RegionalCarbonReportGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportGroupStore(
    var items: MutableMap<String, RegionalCarbonReportGroup>,
    var order: MutableList<String>,
) : RegionalCarbonReportGroupStore {
    override fun find(code: String): RegionalCarbonReportGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportGroup) {
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

    override fun list(): MutableList<RegionalCarbonReportGroup> {
        val out = mutableListOf<RegionalCarbonReportGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportGroupStore(): RegionalCarbonReportGroupStore {
    return MemoryRegionalCarbonReportGroupStore(
        mutableMapOf<String, RegionalCarbonReportGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportGroupStore(
    var inner: RegionalCarbonReportGroupStore,
    var cache: TtlCache<RegionalCarbonReportGroup>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportGroupStore {
    override fun find(code: String): RegionalCarbonReportGroup? {
        val hit = (cache.get(code) as RegionalCarbonReportGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportGroup> = inner.list()
}

fun newCachedRegionalCarbonReportGroupStore(inner: RegionalCarbonReportGroupStore, ttl: Int, clock: Clock): RegionalCarbonReportGroupStore {
    return CachedRegionalCarbonReportGroupStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report groups. Empty fields match all. */
data class RegionalCarbonReportGroupQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportGroup>): MutableList<RegionalCarbonReportGroup> {
        val matched = filter(items) { candidate: RegionalCarbonReportGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportGroups(store: RegionalCarbonReportGroupStore, query: RegionalCarbonReportGroupQuery): MutableList<RegionalCarbonReportGroup> {
    return query.apply(store.list())
}
