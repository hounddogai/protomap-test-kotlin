package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard manifests by code. */
interface RegionalTrailerYardManifestStore {
    fun find(code: String): RegionalTrailerYardManifest?
    fun save(item: RegionalTrailerYardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardManifestStore(
    var items: MutableMap<String, RegionalTrailerYardManifest>,
    var order: MutableList<String>,
) : RegionalTrailerYardManifestStore {
    override fun find(code: String): RegionalTrailerYardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardManifest) {
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

    override fun list(): MutableList<RegionalTrailerYardManifest> {
        val out = mutableListOf<RegionalTrailerYardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardManifestStore(): RegionalTrailerYardManifestStore {
    return MemoryRegionalTrailerYardManifestStore(
        mutableMapOf<String, RegionalTrailerYardManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardManifestStore(
    var inner: RegionalTrailerYardManifestStore,
    var cache: TtlCache<RegionalTrailerYardManifest>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardManifestStore {
    override fun find(code: String): RegionalTrailerYardManifest? {
        val hit = (cache.get(code) as RegionalTrailerYardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardManifest> = inner.list()
}

fun newCachedRegionalTrailerYardManifestStore(inner: RegionalTrailerYardManifestStore, ttl: Int, clock: Clock): RegionalTrailerYardManifestStore {
    return CachedRegionalTrailerYardManifestStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard manifests. Empty fields match all. */
data class RegionalTrailerYardManifestQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RegionalTrailerYardManifest>): MutableList<RegionalTrailerYardManifest> {
        val matched = filter(items) { candidate: RegionalTrailerYardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardManifests(store: RegionalTrailerYardManifestStore, query: RegionalTrailerYardManifestQuery): MutableList<RegionalTrailerYardManifest> {
    return query.apply(store.list())
}
