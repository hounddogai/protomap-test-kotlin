package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire manifests by code. */
interface PrimaryTireManifestStore {
    fun find(code: String): PrimaryTireManifest?
    fun save(item: PrimaryTireManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireManifestStore(
    var items: MutableMap<String, PrimaryTireManifest>,
    var order: MutableList<String>,
) : PrimaryTireManifestStore {
    override fun find(code: String): PrimaryTireManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireManifest) {
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

    override fun list(): MutableList<PrimaryTireManifest> {
        val out = mutableListOf<PrimaryTireManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireManifestStore(): PrimaryTireManifestStore {
    return MemoryPrimaryTireManifestStore(mutableMapOf<String, PrimaryTireManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireManifestStore(
    var inner: PrimaryTireManifestStore,
    var cache: TtlCache<PrimaryTireManifest>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireManifestStore {
    override fun find(code: String): PrimaryTireManifest? {
        val hit = (cache.get(code) as PrimaryTireManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireManifest> = inner.list()
}

fun newCachedPrimaryTireManifestStore(inner: PrimaryTireManifestStore, ttl: Int, clock: Clock): PrimaryTireManifestStore {
    return CachedPrimaryTireManifestStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire manifests. Empty fields match all. */
data class PrimaryTireManifestQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireManifest>): MutableList<PrimaryTireManifest> {
        val matched = filter(items) { candidate: PrimaryTireManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireManifests(store: PrimaryTireManifestStore, query: PrimaryTireManifestQuery): MutableList<PrimaryTireManifest> {
    return query.apply(store.list())
}
