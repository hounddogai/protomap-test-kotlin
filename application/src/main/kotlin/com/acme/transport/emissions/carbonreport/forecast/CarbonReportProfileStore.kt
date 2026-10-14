package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report profiles by code. */
interface CarbonReportProfileStore {
    fun find(code: String): CarbonReportProfile?
    fun save(item: CarbonReportProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportProfileStore(
    var items: MutableMap<String, CarbonReportProfile>,
    var order: MutableList<String>,
) : CarbonReportProfileStore {
    override fun find(code: String): CarbonReportProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportProfile) {
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

    override fun list(): MutableList<CarbonReportProfile> {
        val out = mutableListOf<CarbonReportProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportProfileStore(): CarbonReportProfileStore {
    return MemoryCarbonReportProfileStore(mutableMapOf<String, CarbonReportProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportProfileStore(
    var inner: CarbonReportProfileStore,
    var cache: TtlCache<CarbonReportProfile>,
    var hits: Int,
    var misses: Int,
) : CarbonReportProfileStore {
    override fun find(code: String): CarbonReportProfile? {
        val hit = (cache.get(code) as CarbonReportProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportProfile> = inner.list()
}

fun newCachedCarbonReportProfileStore(inner: CarbonReportProfileStore, ttl: Int, clock: Clock): CarbonReportProfileStore {
    return CachedCarbonReportProfileStore(
        inner = inner,
        cache = newTtlCache<CarbonReportProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report profiles. Empty fields match all. */
data class CarbonReportProfileQuery(
    var region: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportProfile>): MutableList<CarbonReportProfile> {
        val matched = filter(items) { candidate: CarbonReportProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportProfiles(store: CarbonReportProfileStore, query: CarbonReportProfileQuery): MutableList<CarbonReportProfile> {
    return query.apply(store.list())
}
