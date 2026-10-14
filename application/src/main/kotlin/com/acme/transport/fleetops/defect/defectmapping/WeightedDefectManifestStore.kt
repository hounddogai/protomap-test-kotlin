package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted defect manifests by code. */
interface WeightedDefectManifestStore {
    fun find(code: String): WeightedDefectManifest?
    fun save(item: WeightedDefectManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDefectManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDefectManifestStore(
    var items: MutableMap<String, WeightedDefectManifest>,
    var order: MutableList<String>,
) : WeightedDefectManifestStore {
    override fun find(code: String): WeightedDefectManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDefectManifest) {
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

    override fun list(): MutableList<WeightedDefectManifest> {
        val out = mutableListOf<WeightedDefectManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDefectManifestStore(): WeightedDefectManifestStore {
    return MemoryWeightedDefectManifestStore(
        mutableMapOf<String, WeightedDefectManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDefectManifestStore(
    var inner: WeightedDefectManifestStore,
    var cache: TtlCache<WeightedDefectManifest>,
    var hits: Int,
    var misses: Int,
) : WeightedDefectManifestStore {
    override fun find(code: String): WeightedDefectManifest? {
        val hit = (cache.get(code) as WeightedDefectManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDefectManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDefectManifest> = inner.list()
}

fun newCachedWeightedDefectManifestStore(inner: WeightedDefectManifestStore, ttl: Int, clock: Clock): WeightedDefectManifestStore {
    return CachedWeightedDefectManifestStore(
        inner = inner,
        cache = newTtlCache<WeightedDefectManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted defect manifests. Empty fields match all. */
data class WeightedDefectManifestQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDefectManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDefectManifest>): MutableList<WeightedDefectManifest> {
        val matched = filter(items) { candidate: WeightedDefectManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDefectManifests(store: WeightedDefectManifestStore, query: WeightedDefectManifestQuery): MutableList<WeightedDefectManifest> {
    return query.apply(store.list())
}
