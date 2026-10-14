package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy trailer yard manifests by code. */
interface LegacyTrailerYardManifestStore {
    fun find(code: String): LegacyTrailerYardManifest?
    fun save(item: LegacyTrailerYardManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerYardManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerYardManifestStore(
    var items: MutableMap<String, LegacyTrailerYardManifest>,
    var order: MutableList<String>,
) : LegacyTrailerYardManifestStore {
    override fun find(code: String): LegacyTrailerYardManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerYardManifest) {
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

    override fun list(): MutableList<LegacyTrailerYardManifest> {
        val out = mutableListOf<LegacyTrailerYardManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerYardManifestStore(): LegacyTrailerYardManifestStore {
    return MemoryLegacyTrailerYardManifestStore(
        mutableMapOf<String, LegacyTrailerYardManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerYardManifestStore(
    var inner: LegacyTrailerYardManifestStore,
    var cache: TtlCache<LegacyTrailerYardManifest>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerYardManifestStore {
    override fun find(code: String): LegacyTrailerYardManifest? {
        val hit = (cache.get(code) as LegacyTrailerYardManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerYardManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerYardManifest> = inner.list()
}

fun newCachedLegacyTrailerYardManifestStore(inner: LegacyTrailerYardManifestStore, ttl: Int, clock: Clock): LegacyTrailerYardManifestStore {
    return CachedLegacyTrailerYardManifestStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerYardManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer yard manifests. Empty fields match all. */
data class LegacyTrailerYardManifestQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerYardManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerYardManifest>): MutableList<LegacyTrailerYardManifest> {
        val matched = filter(items) { candidate: LegacyTrailerYardManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerYardManifests(store: LegacyTrailerYardManifestStore, query: LegacyTrailerYardManifestQuery): MutableList<LegacyTrailerYardManifest> {
    return query.apply(store.list())
}
