package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire manifests by code. */
interface TireManifestStore {
    fun find(code: String): TireManifest?
    fun save(item: TireManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireManifestStore(
    var items: MutableMap<String, TireManifest>,
    var order: MutableList<String>,
) : TireManifestStore {
    override fun find(code: String): TireManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireManifest) {
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

    override fun list(): MutableList<TireManifest> {
        val out = mutableListOf<TireManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireManifestStore(): TireManifestStore {
    return MemoryTireManifestStore(mutableMapOf<String, TireManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireManifestStore(
    var inner: TireManifestStore,
    var cache: TtlCache<TireManifest>,
    var hits: Int,
    var misses: Int,
) : TireManifestStore {
    override fun find(code: String): TireManifest? {
        val hit = (cache.get(code) as TireManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireManifest> = inner.list()
}

fun newCachedTireManifestStore(inner: TireManifestStore, ttl: Int, clock: Clock): TireManifestStore {
    return CachedTireManifestStore(
        inner = inner,
        cache = newTtlCache<TireManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire manifests. Empty fields match all. */
data class TireManifestQuery(
    var unit: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireManifest>): MutableList<TireManifest> {
        val matched = filter(items) { candidate: TireManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireManifests(store: TireManifestStore, query: TireManifestQuery): MutableList<TireManifest> {
    return query.apply(store.list())
}
