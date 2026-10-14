package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive service plan manifests by code. */
interface AdaptiveServicePlanManifestStore {
    fun find(code: String): AdaptiveServicePlanManifest?
    fun save(item: AdaptiveServicePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServicePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServicePlanManifestStore(
    var items: MutableMap<String, AdaptiveServicePlanManifest>,
    var order: MutableList<String>,
) : AdaptiveServicePlanManifestStore {
    override fun find(code: String): AdaptiveServicePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServicePlanManifest) {
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

    override fun list(): MutableList<AdaptiveServicePlanManifest> {
        val out = mutableListOf<AdaptiveServicePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServicePlanManifestStore(): AdaptiveServicePlanManifestStore {
    return MemoryAdaptiveServicePlanManifestStore(
        mutableMapOf<String, AdaptiveServicePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServicePlanManifestStore(
    var inner: AdaptiveServicePlanManifestStore,
    var cache: TtlCache<AdaptiveServicePlanManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServicePlanManifestStore {
    override fun find(code: String): AdaptiveServicePlanManifest? {
        val hit = (cache.get(code) as AdaptiveServicePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServicePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServicePlanManifest> = inner.list()
}

fun newCachedAdaptiveServicePlanManifestStore(inner: AdaptiveServicePlanManifestStore, ttl: Int, clock: Clock): AdaptiveServicePlanManifestStore {
    return CachedAdaptiveServicePlanManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServicePlanManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service plan manifests. Empty fields match all. */
data class AdaptiveServicePlanManifestQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServicePlanManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<AdaptiveServicePlanManifest>): MutableList<AdaptiveServicePlanManifest> {
        val matched = filter(items) { candidate: AdaptiveServicePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServicePlanManifests(store: AdaptiveServicePlanManifestStore, query: AdaptiveServicePlanManifestQuery): MutableList<AdaptiveServicePlanManifest> {
    return query.apply(store.list())
}
