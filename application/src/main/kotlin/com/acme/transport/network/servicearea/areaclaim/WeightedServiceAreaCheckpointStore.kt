package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted service area checkpoints by code. */
interface WeightedServiceAreaCheckpointStore {
    fun find(code: String): WeightedServiceAreaCheckpoint?
    fun save(item: WeightedServiceAreaCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServiceAreaCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServiceAreaCheckpointStore(
    var items: MutableMap<String, WeightedServiceAreaCheckpoint>,
    var order: MutableList<String>,
) : WeightedServiceAreaCheckpointStore {
    override fun find(code: String): WeightedServiceAreaCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServiceAreaCheckpoint) {
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

    override fun list(): MutableList<WeightedServiceAreaCheckpoint> {
        val out = mutableListOf<WeightedServiceAreaCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServiceAreaCheckpointStore(): WeightedServiceAreaCheckpointStore {
    return MemoryWeightedServiceAreaCheckpointStore(
        mutableMapOf<String, WeightedServiceAreaCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServiceAreaCheckpointStore(
    var inner: WeightedServiceAreaCheckpointStore,
    var cache: TtlCache<WeightedServiceAreaCheckpoint>,
    var hits: Int,
    var misses: Int,
) : WeightedServiceAreaCheckpointStore {
    override fun find(code: String): WeightedServiceAreaCheckpoint? {
        val hit = (cache.get(code) as WeightedServiceAreaCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServiceAreaCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServiceAreaCheckpoint> = inner.list()
}

fun newCachedWeightedServiceAreaCheckpointStore(inner: WeightedServiceAreaCheckpointStore, ttl: Int, clock: Clock): WeightedServiceAreaCheckpointStore {
    return CachedWeightedServiceAreaCheckpointStore(
        inner = inner,
        cache = newTtlCache<WeightedServiceAreaCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service area checkpoints. Empty fields match all. */
data class WeightedServiceAreaCheckpointQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServiceAreaCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServiceAreaCheckpoint>): MutableList<WeightedServiceAreaCheckpoint> {
        val matched = filter(items) { candidate: WeightedServiceAreaCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServiceAreaCheckpoints(store: WeightedServiceAreaCheckpointStore, query: WeightedServiceAreaCheckpointQuery): MutableList<WeightedServiceAreaCheckpoint> {
    return query.apply(store.list())
}
