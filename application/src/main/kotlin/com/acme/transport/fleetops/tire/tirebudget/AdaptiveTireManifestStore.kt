package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tire manifests by code. */
interface AdaptiveTireManifestStore {
    fun find(code: String): AdaptiveTireManifest?
    fun save(item: AdaptiveTireManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTireManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTireManifestStore(
    var items: MutableMap<String, AdaptiveTireManifest>,
    var order: MutableList<String>,
) : AdaptiveTireManifestStore {
    override fun find(code: String): AdaptiveTireManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTireManifest) {
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

    override fun list(): MutableList<AdaptiveTireManifest> {
        val out = mutableListOf<AdaptiveTireManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTireManifestStore(): AdaptiveTireManifestStore {
    return MemoryAdaptiveTireManifestStore(
        mutableMapOf<String, AdaptiveTireManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTireManifestStore(
    var inner: AdaptiveTireManifestStore,
    var cache: TtlCache<AdaptiveTireManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTireManifestStore {
    override fun find(code: String): AdaptiveTireManifest? {
        val hit = (cache.get(code) as AdaptiveTireManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTireManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTireManifest> = inner.list()
}

fun newCachedAdaptiveTireManifestStore(inner: AdaptiveTireManifestStore, ttl: Int, clock: Clock): AdaptiveTireManifestStore {
    return CachedAdaptiveTireManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTireManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tire manifests. Empty fields match all. */
data class AdaptiveTireManifestQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTireManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTireManifest>): MutableList<AdaptiveTireManifest> {
        val matched = filter(items) { candidate: AdaptiveTireManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTireManifests(store: AdaptiveTireManifestStore, query: AdaptiveTireManifestQuery): MutableList<AdaptiveTireManifest> {
    return query.apply(store.list())
}
