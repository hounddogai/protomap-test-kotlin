package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection manifests by code. */
interface AdaptiveInspectionManifestStore {
    fun find(code: String): AdaptiveInspectionManifest?
    fun save(item: AdaptiveInspectionManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionManifestStore(
    var items: MutableMap<String, AdaptiveInspectionManifest>,
    var order: MutableList<String>,
) : AdaptiveInspectionManifestStore {
    override fun find(code: String): AdaptiveInspectionManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionManifest) {
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

    override fun list(): MutableList<AdaptiveInspectionManifest> {
        val out = mutableListOf<AdaptiveInspectionManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionManifestStore(): AdaptiveInspectionManifestStore {
    return MemoryAdaptiveInspectionManifestStore(
        mutableMapOf<String, AdaptiveInspectionManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionManifestStore(
    var inner: AdaptiveInspectionManifestStore,
    var cache: TtlCache<AdaptiveInspectionManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionManifestStore {
    override fun find(code: String): AdaptiveInspectionManifest? {
        val hit = (cache.get(code) as AdaptiveInspectionManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionManifest> = inner.list()
}

fun newCachedAdaptiveInspectionManifestStore(inner: AdaptiveInspectionManifestStore, ttl: Int, clock: Clock): AdaptiveInspectionManifestStore {
    return CachedAdaptiveInspectionManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection manifests. Empty fields match all. */
data class AdaptiveInspectionManifestQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionManifest>): MutableList<AdaptiveInspectionManifest> {
        val matched = filter(items) { candidate: AdaptiveInspectionManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionManifests(store: AdaptiveInspectionManifestStore, query: AdaptiveInspectionManifestQuery): MutableList<AdaptiveInspectionManifest> {
    return query.apply(store.list())
}
