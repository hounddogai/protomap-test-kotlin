package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted zone manifests by code. */
interface WeightedZoneManifestStore {
    fun find(code: String): WeightedZoneManifest?
    fun save(item: WeightedZoneManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedZoneManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedZoneManifestStore(
    var items: MutableMap<String, WeightedZoneManifest>,
    var order: MutableList<String>,
) : WeightedZoneManifestStore {
    override fun find(code: String): WeightedZoneManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedZoneManifest) {
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

    override fun list(): MutableList<WeightedZoneManifest> {
        val out = mutableListOf<WeightedZoneManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedZoneManifestStore(): WeightedZoneManifestStore {
    return MemoryWeightedZoneManifestStore(
        mutableMapOf<String, WeightedZoneManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedZoneManifestStore(
    var inner: WeightedZoneManifestStore,
    var cache: TtlCache<WeightedZoneManifest>,
    var hits: Int,
    var misses: Int,
) : WeightedZoneManifestStore {
    override fun find(code: String): WeightedZoneManifest? {
        val hit = (cache.get(code) as WeightedZoneManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedZoneManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedZoneManifest> = inner.list()
}

fun newCachedWeightedZoneManifestStore(inner: WeightedZoneManifestStore, ttl: Int, clock: Clock): WeightedZoneManifestStore {
    return CachedWeightedZoneManifestStore(
        inner = inner,
        cache = newTtlCache<WeightedZoneManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted zone manifests. Empty fields match all. */
data class WeightedZoneManifestQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedZoneManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedZoneManifest>): MutableList<WeightedZoneManifest> {
        val matched = filter(items) { candidate: WeightedZoneManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedZoneManifests(store: WeightedZoneManifestStore, query: WeightedZoneManifestQuery): MutableList<WeightedZoneManifest> {
    return query.apply(store.list())
}
