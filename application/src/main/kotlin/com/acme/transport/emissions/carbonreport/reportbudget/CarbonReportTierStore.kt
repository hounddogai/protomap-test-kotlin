package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report tiers by code. */
interface CarbonReportTierStore {
    fun find(code: String): CarbonReportTier?
    fun save(item: CarbonReportTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportTierStore(
    var items: MutableMap<String, CarbonReportTier>,
    var order: MutableList<String>,
) : CarbonReportTierStore {
    override fun find(code: String): CarbonReportTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportTier) {
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

    override fun list(): MutableList<CarbonReportTier> {
        val out = mutableListOf<CarbonReportTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportTierStore(): CarbonReportTierStore {
    return MemoryCarbonReportTierStore(mutableMapOf<String, CarbonReportTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportTierStore(
    var inner: CarbonReportTierStore,
    var cache: TtlCache<CarbonReportTier>,
    var hits: Int,
    var misses: Int,
) : CarbonReportTierStore {
    override fun find(code: String): CarbonReportTier? {
        val hit = (cache.get(code) as CarbonReportTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportTier> = inner.list()
}

fun newCachedCarbonReportTierStore(inner: CarbonReportTierStore, ttl: Int, clock: Clock): CarbonReportTierStore {
    return CachedCarbonReportTierStore(
        inner = inner,
        cache = newTtlCache<CarbonReportTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report tiers. Empty fields match all. */
data class CarbonReportTierQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportTier): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportTier>): MutableList<CarbonReportTier> {
        val matched = filter(items) { candidate: CarbonReportTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportTiers(store: CarbonReportTierStore, query: CarbonReportTierQuery): MutableList<CarbonReportTier> {
    return query.apply(store.list())
}
