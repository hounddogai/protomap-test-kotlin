package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted service plan manifests by code. */
interface WeightedServicePlanManifestStore {
    fun find(code: String): WeightedServicePlanManifest?
    fun save(item: WeightedServicePlanManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServicePlanManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServicePlanManifestStore(
    var items: MutableMap<String, WeightedServicePlanManifest>,
    var order: MutableList<String>,
) : WeightedServicePlanManifestStore {
    override fun find(code: String): WeightedServicePlanManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServicePlanManifest) {
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

    override fun list(): MutableList<WeightedServicePlanManifest> {
        val out = mutableListOf<WeightedServicePlanManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServicePlanManifestStore(): WeightedServicePlanManifestStore {
    return MemoryWeightedServicePlanManifestStore(
        mutableMapOf<String, WeightedServicePlanManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServicePlanManifestStore(
    var inner: WeightedServicePlanManifestStore,
    var cache: TtlCache<WeightedServicePlanManifest>,
    var hits: Int,
    var misses: Int,
) : WeightedServicePlanManifestStore {
    override fun find(code: String): WeightedServicePlanManifest? {
        val hit = (cache.get(code) as WeightedServicePlanManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServicePlanManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServicePlanManifest> = inner.list()
}

fun newCachedWeightedServicePlanManifestStore(inner: WeightedServicePlanManifestStore, ttl: Int, clock: Clock): WeightedServicePlanManifestStore {
    return CachedWeightedServicePlanManifestStore(
        inner = inner,
        cache = newTtlCache<WeightedServicePlanManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service plan manifests. Empty fields match all. */
data class WeightedServicePlanManifestQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServicePlanManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServicePlanManifest>): MutableList<WeightedServicePlanManifest> {
        val matched = filter(items) { candidate: WeightedServicePlanManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServicePlanManifests(store: WeightedServicePlanManifestStore, query: WeightedServicePlanManifestQuery): MutableList<WeightedServicePlanManifest> {
    return query.apply(store.list())
}
