package com.acme.transport.crewing.availability.profile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal availability manifests by code. */
interface SeasonalAvailabilityManifestStore {
    fun find(code: String): SeasonalAvailabilityManifest?
    fun save(item: SeasonalAvailabilityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAvailabilityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAvailabilityManifestStore(
    var items: MutableMap<String, SeasonalAvailabilityManifest>,
    var order: MutableList<String>,
) : SeasonalAvailabilityManifestStore {
    override fun find(code: String): SeasonalAvailabilityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAvailabilityManifest) {
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

    override fun list(): MutableList<SeasonalAvailabilityManifest> {
        val out = mutableListOf<SeasonalAvailabilityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAvailabilityManifestStore(): SeasonalAvailabilityManifestStore {
    return MemorySeasonalAvailabilityManifestStore(
        mutableMapOf<String, SeasonalAvailabilityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAvailabilityManifestStore(
    var inner: SeasonalAvailabilityManifestStore,
    var cache: TtlCache<SeasonalAvailabilityManifest>,
    var hits: Int,
    var misses: Int,
) : SeasonalAvailabilityManifestStore {
    override fun find(code: String): SeasonalAvailabilityManifest? {
        val hit = (cache.get(code) as SeasonalAvailabilityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAvailabilityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAvailabilityManifest> = inner.list()
}

fun newCachedSeasonalAvailabilityManifestStore(inner: SeasonalAvailabilityManifestStore, ttl: Int, clock: Clock): SeasonalAvailabilityManifestStore {
    return CachedSeasonalAvailabilityManifestStore(
        inner = inner,
        cache = newTtlCache<SeasonalAvailabilityManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal availability manifests. Empty fields match all. */
data class SeasonalAvailabilityManifestQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAvailabilityManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAvailabilityManifest>): MutableList<SeasonalAvailabilityManifest> {
        val matched = filter(items) { candidate: SeasonalAvailabilityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAvailabilityManifests(store: SeasonalAvailabilityManifestStore, query: SeasonalAvailabilityManifestQuery): MutableList<SeasonalAvailabilityManifest> {
    return query.apply(store.list())
}
