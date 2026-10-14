package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn checkpoints by code. */
interface FuelBurnCheckpointStore {
    fun find(code: String): FuelBurnCheckpoint?
    fun save(item: FuelBurnCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnCheckpointStore(
    var items: MutableMap<String, FuelBurnCheckpoint>,
    var order: MutableList<String>,
) : FuelBurnCheckpointStore {
    override fun find(code: String): FuelBurnCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnCheckpoint) {
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

    override fun list(): MutableList<FuelBurnCheckpoint> {
        val out = mutableListOf<FuelBurnCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnCheckpointStore(): FuelBurnCheckpointStore {
    return MemoryFuelBurnCheckpointStore(mutableMapOf<String, FuelBurnCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnCheckpointStore(
    var inner: FuelBurnCheckpointStore,
    var cache: TtlCache<FuelBurnCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FuelBurnCheckpointStore {
    override fun find(code: String): FuelBurnCheckpoint? {
        val hit = (cache.get(code) as FuelBurnCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnCheckpoint> = inner.list()
}

fun newCachedFuelBurnCheckpointStore(inner: FuelBurnCheckpointStore, ttl: Int, clock: Clock): FuelBurnCheckpointStore {
    return CachedFuelBurnCheckpointStore(
        inner = inner,
        cache = newTtlCache<FuelBurnCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn checkpoints. Empty fields match all. */
data class FuelBurnCheckpointQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnCheckpoint>): MutableList<FuelBurnCheckpoint> {
        val matched = filter(items) { candidate: FuelBurnCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnCheckpoints(store: FuelBurnCheckpointStore, query: FuelBurnCheckpointQuery): MutableList<FuelBurnCheckpoint> {
    return query.apply(store.list())
}
