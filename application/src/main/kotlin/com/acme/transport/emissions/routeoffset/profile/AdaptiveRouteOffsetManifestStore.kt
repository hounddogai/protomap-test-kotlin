package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive route offset manifests by code. */
interface AdaptiveRouteOffsetManifestStore {
    fun find(code: String): AdaptiveRouteOffsetManifest?
    fun save(item: AdaptiveRouteOffsetManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRouteOffsetManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRouteOffsetManifestStore(
    var items: MutableMap<String, AdaptiveRouteOffsetManifest>,
    var order: MutableList<String>,
) : AdaptiveRouteOffsetManifestStore {
    override fun find(code: String): AdaptiveRouteOffsetManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRouteOffsetManifest) {
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

    override fun list(): MutableList<AdaptiveRouteOffsetManifest> {
        val out = mutableListOf<AdaptiveRouteOffsetManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRouteOffsetManifestStore(): AdaptiveRouteOffsetManifestStore {
    return MemoryAdaptiveRouteOffsetManifestStore(
        mutableMapOf<String, AdaptiveRouteOffsetManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRouteOffsetManifestStore(
    var inner: AdaptiveRouteOffsetManifestStore,
    var cache: TtlCache<AdaptiveRouteOffsetManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRouteOffsetManifestStore {
    override fun find(code: String): AdaptiveRouteOffsetManifest? {
        val hit = (cache.get(code) as AdaptiveRouteOffsetManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRouteOffsetManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRouteOffsetManifest> = inner.list()
}

fun newCachedAdaptiveRouteOffsetManifestStore(inner: AdaptiveRouteOffsetManifestStore, ttl: Int, clock: Clock): AdaptiveRouteOffsetManifestStore {
    return CachedAdaptiveRouteOffsetManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRouteOffsetManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive route offset manifests. Empty fields match all. */
data class AdaptiveRouteOffsetManifestQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRouteOffsetManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AdaptiveRouteOffsetManifest>): MutableList<AdaptiveRouteOffsetManifest> {
        val matched = filter(items) { candidate: AdaptiveRouteOffsetManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRouteOffsetManifests(store: AdaptiveRouteOffsetManifestStore, query: AdaptiveRouteOffsetManifestQuery): MutableList<AdaptiveRouteOffsetManifest> {
    return query.apply(store.list())
}
