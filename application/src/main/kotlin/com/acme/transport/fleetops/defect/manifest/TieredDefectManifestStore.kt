package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered defect manifests by code. */
interface TieredDefectManifestStore {
    fun find(code: String): TieredDefectManifest?
    fun save(item: TieredDefectManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDefectManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDefectManifestStore(
    var items: MutableMap<String, TieredDefectManifest>,
    var order: MutableList<String>,
) : TieredDefectManifestStore {
    override fun find(code: String): TieredDefectManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDefectManifest) {
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

    override fun list(): MutableList<TieredDefectManifest> {
        val out = mutableListOf<TieredDefectManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDefectManifestStore(): TieredDefectManifestStore {
    return MemoryTieredDefectManifestStore(
        mutableMapOf<String, TieredDefectManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDefectManifestStore(
    var inner: TieredDefectManifestStore,
    var cache: TtlCache<TieredDefectManifest>,
    var hits: Int,
    var misses: Int,
) : TieredDefectManifestStore {
    override fun find(code: String): TieredDefectManifest? {
        val hit = (cache.get(code) as TieredDefectManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDefectManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDefectManifest> = inner.list()
}

fun newCachedTieredDefectManifestStore(inner: TieredDefectManifestStore, ttl: Int, clock: Clock): TieredDefectManifestStore {
    return CachedTieredDefectManifestStore(
        inner = inner,
        cache = newTtlCache<TieredDefectManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered defect manifests. Empty fields match all. */
data class TieredDefectManifestQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDefectManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDefectManifest>): MutableList<TieredDefectManifest> {
        val matched = filter(items) { candidate: TieredDefectManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDefectManifests(store: TieredDefectManifestStore, query: TieredDefectManifestQuery): MutableList<TieredDefectManifest> {
    return query.apply(store.list())
}
