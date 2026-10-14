package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict trailer manifests by code. */
interface StrictTrailerManifestStore {
    fun find(code: String): StrictTrailerManifest?
    fun save(item: StrictTrailerManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerManifestStore(
    var items: MutableMap<String, StrictTrailerManifest>,
    var order: MutableList<String>,
) : StrictTrailerManifestStore {
    override fun find(code: String): StrictTrailerManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerManifest) {
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

    override fun list(): MutableList<StrictTrailerManifest> {
        val out = mutableListOf<StrictTrailerManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerManifestStore(): StrictTrailerManifestStore {
    return MemoryStrictTrailerManifestStore(
        mutableMapOf<String, StrictTrailerManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerManifestStore(
    var inner: StrictTrailerManifestStore,
    var cache: TtlCache<StrictTrailerManifest>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerManifestStore {
    override fun find(code: String): StrictTrailerManifest? {
        val hit = (cache.get(code) as StrictTrailerManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerManifest> = inner.list()
}

fun newCachedStrictTrailerManifestStore(inner: StrictTrailerManifestStore, ttl: Int, clock: Clock): StrictTrailerManifestStore {
    return CachedStrictTrailerManifestStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer manifests. Empty fields match all. */
data class StrictTrailerManifestQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerManifest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTrailerManifest>): MutableList<StrictTrailerManifest> {
        val matched = filter(items) { candidate: StrictTrailerManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerManifests(store: StrictTrailerManifestStore, query: StrictTrailerManifestQuery): MutableList<StrictTrailerManifest> {
    return query.apply(store.list())
}
