package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted proof of delivery manifests by code. */
interface WeightedProofOfDeliveryManifestStore {
    fun find(code: String): WeightedProofOfDeliveryManifest?
    fun save(item: WeightedProofOfDeliveryManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedProofOfDeliveryManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedProofOfDeliveryManifestStore(
    var items: MutableMap<String, WeightedProofOfDeliveryManifest>,
    var order: MutableList<String>,
) : WeightedProofOfDeliveryManifestStore {
    override fun find(code: String): WeightedProofOfDeliveryManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedProofOfDeliveryManifest) {
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

    override fun list(): MutableList<WeightedProofOfDeliveryManifest> {
        val out = mutableListOf<WeightedProofOfDeliveryManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedProofOfDeliveryManifestStore(): WeightedProofOfDeliveryManifestStore {
    return MemoryWeightedProofOfDeliveryManifestStore(
        mutableMapOf<String, WeightedProofOfDeliveryManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedProofOfDeliveryManifestStore(
    var inner: WeightedProofOfDeliveryManifestStore,
    var cache: TtlCache<WeightedProofOfDeliveryManifest>,
    var hits: Int,
    var misses: Int,
) : WeightedProofOfDeliveryManifestStore {
    override fun find(code: String): WeightedProofOfDeliveryManifest? {
        val hit = (cache.get(code) as WeightedProofOfDeliveryManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedProofOfDeliveryManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedProofOfDeliveryManifest> = inner.list()
}

fun newCachedWeightedProofOfDeliveryManifestStore(inner: WeightedProofOfDeliveryManifestStore, ttl: Int, clock: Clock): WeightedProofOfDeliveryManifestStore {
    return CachedWeightedProofOfDeliveryManifestStore(
        inner = inner,
        cache = newTtlCache<WeightedProofOfDeliveryManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted proof of delivery manifests. Empty fields match all. */
data class WeightedProofOfDeliveryManifestQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedProofOfDeliveryManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedProofOfDeliveryManifest>): MutableList<WeightedProofOfDeliveryManifest> {
        val matched = filter(items) { candidate: WeightedProofOfDeliveryManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedProofOfDeliveryManifests(store: WeightedProofOfDeliveryManifestStore, query: WeightedProofOfDeliveryManifestQuery): MutableList<WeightedProofOfDeliveryManifest> {
    return query.apply(store.list())
}
