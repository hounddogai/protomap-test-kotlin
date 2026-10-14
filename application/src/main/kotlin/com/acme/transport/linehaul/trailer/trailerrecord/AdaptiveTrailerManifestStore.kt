package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive trailer manifests by code. */
interface AdaptiveTrailerManifestStore {
    fun find(code: String): AdaptiveTrailerManifest?
    fun save(item: AdaptiveTrailerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTrailerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTrailerManifestStore(
    var items: MutableMap<String, AdaptiveTrailerManifest>,
    var order: MutableList<String>,
) : AdaptiveTrailerManifestStore {
    override fun find(code: String): AdaptiveTrailerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTrailerManifest) {
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

    override fun list(): MutableList<AdaptiveTrailerManifest> {
        val out = mutableListOf<AdaptiveTrailerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTrailerManifestStore(): AdaptiveTrailerManifestStore {
    return MemoryAdaptiveTrailerManifestStore(
        mutableMapOf<String, AdaptiveTrailerManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTrailerManifestStore(
    var inner: AdaptiveTrailerManifestStore,
    var cache: TtlCache<AdaptiveTrailerManifest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTrailerManifestStore {
    override fun find(code: String): AdaptiveTrailerManifest? {
        val hit = (cache.get(code) as AdaptiveTrailerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTrailerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTrailerManifest> = inner.list()
}

fun newCachedAdaptiveTrailerManifestStore(inner: AdaptiveTrailerManifestStore, ttl: Int, clock: Clock): AdaptiveTrailerManifestStore {
    return CachedAdaptiveTrailerManifestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTrailerManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive trailer manifests. Empty fields match all. */
data class AdaptiveTrailerManifestQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTrailerManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTrailerManifest>): MutableList<AdaptiveTrailerManifest> {
        val matched = filter(items) { candidate: AdaptiveTrailerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTrailerManifests(store: AdaptiveTrailerManifestStore, query: AdaptiveTrailerManifestQuery): MutableList<AdaptiveTrailerManifest> {
    return query.apply(store.list())
}
