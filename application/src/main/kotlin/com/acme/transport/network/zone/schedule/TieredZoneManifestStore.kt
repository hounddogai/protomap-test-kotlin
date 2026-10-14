package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered zone manifests by code. */
interface TieredZoneManifestStore {
    fun find(code: String): TieredZoneManifest?
    fun save(item: TieredZoneManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredZoneManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredZoneManifestStore(
    var items: MutableMap<String, TieredZoneManifest>,
    var order: MutableList<String>,
) : TieredZoneManifestStore {
    override fun find(code: String): TieredZoneManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredZoneManifest) {
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

    override fun list(): MutableList<TieredZoneManifest> {
        val out = mutableListOf<TieredZoneManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredZoneManifestStore(): TieredZoneManifestStore {
    return MemoryTieredZoneManifestStore(mutableMapOf<String, TieredZoneManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredZoneManifestStore(
    var inner: TieredZoneManifestStore,
    var cache: TtlCache<TieredZoneManifest>,
    var hits: Int,
    var misses: Int,
) : TieredZoneManifestStore {
    override fun find(code: String): TieredZoneManifest? {
        val hit = (cache.get(code) as TieredZoneManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredZoneManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredZoneManifest> = inner.list()
}

fun newCachedTieredZoneManifestStore(inner: TieredZoneManifestStore, ttl: Int, clock: Clock): TieredZoneManifestStore {
    return CachedTieredZoneManifestStore(
        inner = inner,
        cache = newTtlCache<TieredZoneManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered zone manifests. Empty fields match all. */
data class TieredZoneManifestQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredZoneManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredZoneManifest>): MutableList<TieredZoneManifest> {
        val matched = filter(items) { candidate: TieredZoneManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredZoneManifests(store: TieredZoneManifestStore, query: TieredZoneManifestQuery): MutableList<TieredZoneManifest> {
    return query.apply(store.list())
}
