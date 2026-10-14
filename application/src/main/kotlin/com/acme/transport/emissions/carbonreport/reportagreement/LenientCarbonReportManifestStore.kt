package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient carbon report manifests by code. */
interface LenientCarbonReportManifestStore {
    fun find(code: String): LenientCarbonReportManifest?
    fun save(item: LenientCarbonReportManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCarbonReportManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCarbonReportManifestStore(
    var items: MutableMap<String, LenientCarbonReportManifest>,
    var order: MutableList<String>,
) : LenientCarbonReportManifestStore {
    override fun find(code: String): LenientCarbonReportManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCarbonReportManifest) {
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

    override fun list(): MutableList<LenientCarbonReportManifest> {
        val out = mutableListOf<LenientCarbonReportManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCarbonReportManifestStore(): LenientCarbonReportManifestStore {
    return MemoryLenientCarbonReportManifestStore(
        mutableMapOf<String, LenientCarbonReportManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCarbonReportManifestStore(
    var inner: LenientCarbonReportManifestStore,
    var cache: TtlCache<LenientCarbonReportManifest>,
    var hits: Int,
    var misses: Int,
) : LenientCarbonReportManifestStore {
    override fun find(code: String): LenientCarbonReportManifest? {
        val hit = (cache.get(code) as LenientCarbonReportManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCarbonReportManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCarbonReportManifest> = inner.list()
}

fun newCachedLenientCarbonReportManifestStore(inner: LenientCarbonReportManifestStore, ttl: Int, clock: Clock): LenientCarbonReportManifestStore {
    return CachedLenientCarbonReportManifestStore(
        inner = inner,
        cache = newTtlCache<LenientCarbonReportManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient carbon report manifests. Empty fields match all. */
data class LenientCarbonReportManifestQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCarbonReportManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LenientCarbonReportManifest>): MutableList<LenientCarbonReportManifest> {
        val matched = filter(items) { candidate: LenientCarbonReportManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCarbonReportManifests(store: LenientCarbonReportManifestStore, query: LenientCarbonReportManifestQuery): MutableList<LenientCarbonReportManifest> {
    return query.apply(store.list())
}
