package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer manifests by code. */
interface TrailerManifestStore {
    fun find(code: String): TrailerManifest?
    fun save(item: TrailerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerManifestStore(
    var items: MutableMap<String, TrailerManifest>,
    var order: MutableList<String>,
) : TrailerManifestStore {
    override fun find(code: String): TrailerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerManifest) {
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

    override fun list(): MutableList<TrailerManifest> {
        val out = mutableListOf<TrailerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerManifestStore(): TrailerManifestStore {
    return MemoryTrailerManifestStore(mutableMapOf<String, TrailerManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerManifestStore(
    var inner: TrailerManifestStore,
    var cache: TtlCache<TrailerManifest>,
    var hits: Int,
    var misses: Int,
) : TrailerManifestStore {
    override fun find(code: String): TrailerManifest? {
        val hit = (cache.get(code) as TrailerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerManifest> = inner.list()
}

fun newCachedTrailerManifestStore(inner: TrailerManifestStore, ttl: Int, clock: Clock): TrailerManifestStore {
    return CachedTrailerManifestStore(
        inner = inner,
        cache = newTtlCache<TrailerManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer manifests. Empty fields match all. */
data class TrailerManifestQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerManifest>): MutableList<TrailerManifest> {
        val matched = filter(items) { candidate: TrailerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerManifests(store: TrailerManifestStore, query: TrailerManifestQuery): MutableList<TrailerManifest> {
    return query.apply(store.list())
}
