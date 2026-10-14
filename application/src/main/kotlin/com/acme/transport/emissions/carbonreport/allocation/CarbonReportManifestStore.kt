package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report manifests by code. */
interface CarbonReportManifestStore {
    fun find(code: String): CarbonReportManifest?
    fun save(item: CarbonReportManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportManifestStore(
    var items: MutableMap<String, CarbonReportManifest>,
    var order: MutableList<String>,
) : CarbonReportManifestStore {
    override fun find(code: String): CarbonReportManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportManifest) {
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

    override fun list(): MutableList<CarbonReportManifest> {
        val out = mutableListOf<CarbonReportManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportManifestStore(): CarbonReportManifestStore {
    return MemoryCarbonReportManifestStore(
        mutableMapOf<String, CarbonReportManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportManifestStore(
    var inner: CarbonReportManifestStore,
    var cache: TtlCache<CarbonReportManifest>,
    var hits: Int,
    var misses: Int,
) : CarbonReportManifestStore {
    override fun find(code: String): CarbonReportManifest? {
        val hit = (cache.get(code) as CarbonReportManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportManifest> = inner.list()
}

fun newCachedCarbonReportManifestStore(inner: CarbonReportManifestStore, ttl: Int, clock: Clock): CarbonReportManifestStore {
    return CachedCarbonReportManifestStore(
        inner = inner,
        cache = newTtlCache<CarbonReportManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report manifests. Empty fields match all. */
data class CarbonReportManifestQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportManifest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportManifest>): MutableList<CarbonReportManifest> {
        val matched = filter(items) { candidate: CarbonReportManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportManifests(store: CarbonReportManifestStore, query: CarbonReportManifestQuery): MutableList<CarbonReportManifest> {
    return query.apply(store.list())
}
