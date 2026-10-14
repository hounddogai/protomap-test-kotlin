package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal backhaul manifests by code. */
interface SeasonalBackhaulManifestStore {
    fun find(code: String): SeasonalBackhaulManifest?
    fun save(item: SeasonalBackhaulManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalBackhaulManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalBackhaulManifestStore(
    var items: MutableMap<String, SeasonalBackhaulManifest>,
    var order: MutableList<String>,
) : SeasonalBackhaulManifestStore {
    override fun find(code: String): SeasonalBackhaulManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalBackhaulManifest) {
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

    override fun list(): MutableList<SeasonalBackhaulManifest> {
        val out = mutableListOf<SeasonalBackhaulManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalBackhaulManifestStore(): SeasonalBackhaulManifestStore {
    return MemorySeasonalBackhaulManifestStore(
        mutableMapOf<String, SeasonalBackhaulManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalBackhaulManifestStore(
    var inner: SeasonalBackhaulManifestStore,
    var cache: TtlCache<SeasonalBackhaulManifest>,
    var hits: Int,
    var misses: Int,
) : SeasonalBackhaulManifestStore {
    override fun find(code: String): SeasonalBackhaulManifest? {
        val hit = (cache.get(code) as SeasonalBackhaulManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalBackhaulManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalBackhaulManifest> = inner.list()
}

fun newCachedSeasonalBackhaulManifestStore(inner: SeasonalBackhaulManifestStore, ttl: Int, clock: Clock): SeasonalBackhaulManifestStore {
    return CachedSeasonalBackhaulManifestStore(
        inner = inner,
        cache = newTtlCache<SeasonalBackhaulManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal backhaul manifests. Empty fields match all. */
data class SeasonalBackhaulManifestQuery(
    var source: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalBackhaulManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalBackhaulManifest>): MutableList<SeasonalBackhaulManifest> {
        val matched = filter(items) { candidate: SeasonalBackhaulManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalBackhaulManifests(store: SeasonalBackhaulManifestStore, query: SeasonalBackhaulManifestQuery): MutableList<SeasonalBackhaulManifest> {
    return query.apply(store.list())
}
