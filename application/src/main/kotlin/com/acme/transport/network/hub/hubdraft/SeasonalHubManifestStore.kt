package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal hub manifests by code. */
interface SeasonalHubManifestStore {
    fun find(code: String): SeasonalHubManifest?
    fun save(item: SeasonalHubManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalHubManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalHubManifestStore(
    var items: MutableMap<String, SeasonalHubManifest>,
    var order: MutableList<String>,
) : SeasonalHubManifestStore {
    override fun find(code: String): SeasonalHubManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalHubManifest) {
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

    override fun list(): MutableList<SeasonalHubManifest> {
        val out = mutableListOf<SeasonalHubManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalHubManifestStore(): SeasonalHubManifestStore {
    return MemorySeasonalHubManifestStore(mutableMapOf<String, SeasonalHubManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalHubManifestStore(
    var inner: SeasonalHubManifestStore,
    var cache: TtlCache<SeasonalHubManifest>,
    var hits: Int,
    var misses: Int,
) : SeasonalHubManifestStore {
    override fun find(code: String): SeasonalHubManifest? {
        val hit = (cache.get(code) as SeasonalHubManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalHubManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalHubManifest> = inner.list()
}

fun newCachedSeasonalHubManifestStore(inner: SeasonalHubManifestStore, ttl: Int, clock: Clock): SeasonalHubManifestStore {
    return CachedSeasonalHubManifestStore(
        inner = inner,
        cache = newTtlCache<SeasonalHubManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal hub manifests. Empty fields match all. */
data class SeasonalHubManifestQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalHubManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalHubManifest>): MutableList<SeasonalHubManifest> {
        val matched = filter(items) { candidate: SeasonalHubManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalHubManifests(store: SeasonalHubManifestStore, query: SeasonalHubManifestQuery): MutableList<SeasonalHubManifest> {
    return query.apply(store.list())
}
