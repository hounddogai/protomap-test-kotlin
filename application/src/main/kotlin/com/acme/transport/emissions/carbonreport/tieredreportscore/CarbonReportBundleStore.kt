package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report bundles by code. */
interface CarbonReportBundleStore {
    fun find(code: String): CarbonReportBundle?
    fun save(item: CarbonReportBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportBundleStore(
    var items: MutableMap<String, CarbonReportBundle>,
    var order: MutableList<String>,
) : CarbonReportBundleStore {
    override fun find(code: String): CarbonReportBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportBundle) {
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

    override fun list(): MutableList<CarbonReportBundle> {
        val out = mutableListOf<CarbonReportBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportBundleStore(): CarbonReportBundleStore {
    return MemoryCarbonReportBundleStore(mutableMapOf<String, CarbonReportBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportBundleStore(
    var inner: CarbonReportBundleStore,
    var cache: TtlCache<CarbonReportBundle>,
    var hits: Int,
    var misses: Int,
) : CarbonReportBundleStore {
    override fun find(code: String): CarbonReportBundle? {
        val hit = (cache.get(code) as CarbonReportBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportBundle> = inner.list()
}

fun newCachedCarbonReportBundleStore(inner: CarbonReportBundleStore, ttl: Int, clock: Clock): CarbonReportBundleStore {
    return CachedCarbonReportBundleStore(
        inner = inner,
        cache = newTtlCache<CarbonReportBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report bundles. Empty fields match all. */
data class CarbonReportBundleQuery(
    var channel: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportBundle>): MutableList<CarbonReportBundle> {
        val matched = filter(items) { candidate: CarbonReportBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportBundles(store: CarbonReportBundleStore, query: CarbonReportBundleQuery): MutableList<CarbonReportBundle> {
    return query.apply(store.list())
}
