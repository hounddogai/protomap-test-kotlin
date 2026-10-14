package com.acme.transport.crewing.availability.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive availability manifests by code. */
interface AdaptiveAvailabilityManifestStore {
    fun find(code: String): AdaptiveAvailabilityManifest?
    fun save(item: AdaptiveAvailabilityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAvailabilityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAvailabilityManifestStore(
    var items: MutableMap<String, AdaptiveAvailabilityManifest>,
    var order: MutableList<String>,
) : AdaptiveAvailabilityManifestStore {
    override fun find(code: String): AdaptiveAvailabilityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAvailabilityManifest) {
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

    override fun list(): MutableList<AdaptiveAvailabilityManifest> {
        val out = mutableListOf<AdaptiveAvailabilityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAvailabilityManifestStore(): AdaptiveAvailabilityManifestStore {
    return MemoryAdaptiveAvailabilityManifestStore(
        mutableMapOf<String, AdaptiveAvailabilityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAvailabilityManifestStore(
    var inner: AdaptiveAvailabilityManifestStore,
    var cache: TtlCache<AdaptiveAvailabilityManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAvailabilityManifestStore {
    override fun find(code: String): AdaptiveAvailabilityManifest? {
        val hit = (cache.get(code) as AdaptiveAvailabilityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAvailabilityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAvailabilityManifest> = inner.list()
}

fun newCachedAdaptiveAvailabilityManifestStore(inner: AdaptiveAvailabilityManifestStore, ttl: Int, clock: Clock): AdaptiveAvailabilityManifestStore {
    return CachedAdaptiveAvailabilityManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAvailabilityManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive availability manifests. Empty fields match all. */
data class AdaptiveAvailabilityManifestQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAvailabilityManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAvailabilityManifest>): MutableList<AdaptiveAvailabilityManifest> {
        val matched = filter(items) { candidate: AdaptiveAvailabilityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAvailabilityManifests(store: AdaptiveAvailabilityManifestStore, query: AdaptiveAvailabilityManifestQuery): MutableList<AdaptiveAvailabilityManifest> {
    return query.apply(store.list())
}
