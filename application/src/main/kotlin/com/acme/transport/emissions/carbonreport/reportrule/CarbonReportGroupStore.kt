package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report groups by code. */
interface CarbonReportGroupStore {
    fun find(code: String): CarbonReportGroup?
    fun save(item: CarbonReportGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportGroupStore(
    var items: MutableMap<String, CarbonReportGroup>,
    var order: MutableList<String>,
) : CarbonReportGroupStore {
    override fun find(code: String): CarbonReportGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportGroup) {
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

    override fun list(): MutableList<CarbonReportGroup> {
        val out = mutableListOf<CarbonReportGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportGroupStore(): CarbonReportGroupStore {
    return MemoryCarbonReportGroupStore(mutableMapOf<String, CarbonReportGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportGroupStore(
    var inner: CarbonReportGroupStore,
    var cache: TtlCache<CarbonReportGroup>,
    var hits: Int,
    var misses: Int,
) : CarbonReportGroupStore {
    override fun find(code: String): CarbonReportGroup? {
        val hit = (cache.get(code) as CarbonReportGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportGroup> = inner.list()
}

fun newCachedCarbonReportGroupStore(inner: CarbonReportGroupStore, ttl: Int, clock: Clock): CarbonReportGroupStore {
    return CachedCarbonReportGroupStore(
        inner = inner,
        cache = newTtlCache<CarbonReportGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report groups. Empty fields match all. */
data class CarbonReportGroupQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportGroup>): MutableList<CarbonReportGroup> {
        val matched = filter(items) { candidate: CarbonReportGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportGroups(store: CarbonReportGroupStore, query: CarbonReportGroupQuery): MutableList<CarbonReportGroup> {
    return query.apply(store.list())
}
