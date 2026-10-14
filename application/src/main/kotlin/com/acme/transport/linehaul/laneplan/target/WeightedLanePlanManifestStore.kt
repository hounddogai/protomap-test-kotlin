package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan manifests by code. */
interface WeightedLanePlanManifestStore {
    fun find(code: String): WeightedLanePlanManifest?
    fun save(item: WeightedLanePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanManifestStore(
    var items: MutableMap<String, WeightedLanePlanManifest>,
    var order: MutableList<String>,
) : WeightedLanePlanManifestStore {
    override fun find(code: String): WeightedLanePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanManifest) {
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

    override fun list(): MutableList<WeightedLanePlanManifest> {
        val out = mutableListOf<WeightedLanePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanManifestStore(): WeightedLanePlanManifestStore {
    return MemoryWeightedLanePlanManifestStore(
        mutableMapOf<String, WeightedLanePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanManifestStore(
    var inner: WeightedLanePlanManifestStore,
    var cache: TtlCache<WeightedLanePlanManifest>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanManifestStore {
    override fun find(code: String): WeightedLanePlanManifest? {
        val hit = (cache.get(code) as WeightedLanePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanManifest> = inner.list()
}

fun newCachedWeightedLanePlanManifestStore(inner: WeightedLanePlanManifestStore, ttl: Int, clock: Clock): WeightedLanePlanManifestStore {
    return CachedWeightedLanePlanManifestStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan manifests. Empty fields match all. */
data class WeightedLanePlanManifestQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanManifest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<WeightedLanePlanManifest>): MutableList<WeightedLanePlanManifest> {
        val matched = filter(items) { candidate: WeightedLanePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanManifests(store: WeightedLanePlanManifestStore, query: WeightedLanePlanManifestQuery): MutableList<WeightedLanePlanManifest> {
    return query.apply(store.list())
}
