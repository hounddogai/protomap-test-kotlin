package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped carbon report groups by code. */
interface CappedCarbonReportGroupStore {
    fun find(code: String): CappedCarbonReportGroup?
    fun save(item: CappedCarbonReportGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCarbonReportGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCarbonReportGroupStore(
    var items: MutableMap<String, CappedCarbonReportGroup>,
    var order: MutableList<String>,
) : CappedCarbonReportGroupStore {
    override fun find(code: String): CappedCarbonReportGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCarbonReportGroup) {
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

    override fun list(): MutableList<CappedCarbonReportGroup> {
        val out = mutableListOf<CappedCarbonReportGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCarbonReportGroupStore(): CappedCarbonReportGroupStore {
    return MemoryCappedCarbonReportGroupStore(
        mutableMapOf<String, CappedCarbonReportGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCarbonReportGroupStore(
    var inner: CappedCarbonReportGroupStore,
    var cache: TtlCache<CappedCarbonReportGroup>,
    var hits: Int,
    var misses: Int,
) : CappedCarbonReportGroupStore {
    override fun find(code: String): CappedCarbonReportGroup? {
        val hit = (cache.get(code) as CappedCarbonReportGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCarbonReportGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCarbonReportGroup> = inner.list()
}

fun newCachedCappedCarbonReportGroupStore(inner: CappedCarbonReportGroupStore, ttl: Int, clock: Clock): CappedCarbonReportGroupStore {
    return CachedCappedCarbonReportGroupStore(
        inner = inner,
        cache = newTtlCache<CappedCarbonReportGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped carbon report groups. Empty fields match all. */
data class CappedCarbonReportGroupQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCarbonReportGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCarbonReportGroup>): MutableList<CappedCarbonReportGroup> {
        val matched = filter(items) { candidate: CappedCarbonReportGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCarbonReportGroups(store: CappedCarbonReportGroupStore, query: CappedCarbonReportGroupQuery): MutableList<CappedCarbonReportGroup> {
    return query.apply(store.list())
}
