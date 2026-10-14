package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive attempt manifests by code. */
interface AdaptiveAttemptManifestStore {
    fun find(code: String): AdaptiveAttemptManifest?
    fun save(item: AdaptiveAttemptManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAttemptManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAttemptManifestStore(
    var items: MutableMap<String, AdaptiveAttemptManifest>,
    var order: MutableList<String>,
) : AdaptiveAttemptManifestStore {
    override fun find(code: String): AdaptiveAttemptManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAttemptManifest) {
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

    override fun list(): MutableList<AdaptiveAttemptManifest> {
        val out = mutableListOf<AdaptiveAttemptManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAttemptManifestStore(): AdaptiveAttemptManifestStore {
    return MemoryAdaptiveAttemptManifestStore(
        mutableMapOf<String, AdaptiveAttemptManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAttemptManifestStore(
    var inner: AdaptiveAttemptManifestStore,
    var cache: TtlCache<AdaptiveAttemptManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAttemptManifestStore {
    override fun find(code: String): AdaptiveAttemptManifest? {
        val hit = (cache.get(code) as AdaptiveAttemptManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAttemptManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAttemptManifest> = inner.list()
}

fun newCachedAdaptiveAttemptManifestStore(inner: AdaptiveAttemptManifestStore, ttl: Int, clock: Clock): AdaptiveAttemptManifestStore {
    return CachedAdaptiveAttemptManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAttemptManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive attempt manifests. Empty fields match all. */
data class AdaptiveAttemptManifestQuery(
    var region: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAttemptManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAttemptManifest>): MutableList<AdaptiveAttemptManifest> {
        val matched = filter(items) { candidate: AdaptiveAttemptManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAttemptManifests(store: AdaptiveAttemptManifestStore, query: AdaptiveAttemptManifestQuery): MutableList<AdaptiveAttemptManifest> {
    return query.apply(store.list())
}
