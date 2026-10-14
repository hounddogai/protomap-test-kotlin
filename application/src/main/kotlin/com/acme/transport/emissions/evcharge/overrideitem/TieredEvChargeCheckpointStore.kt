package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge checkpoints by code. */
interface TieredEvChargeCheckpointStore {
    fun find(code: String): TieredEvChargeCheckpoint?
    fun save(item: TieredEvChargeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargeCheckpointStore(
    var items: MutableMap<String, TieredEvChargeCheckpoint>,
    var order: MutableList<String>,
) : TieredEvChargeCheckpointStore {
    override fun find(code: String): TieredEvChargeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargeCheckpoint) {
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

    override fun list(): MutableList<TieredEvChargeCheckpoint> {
        val out = mutableListOf<TieredEvChargeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargeCheckpointStore(): TieredEvChargeCheckpointStore {
    return MemoryTieredEvChargeCheckpointStore(
        mutableMapOf<String, TieredEvChargeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargeCheckpointStore(
    var inner: TieredEvChargeCheckpointStore,
    var cache: TtlCache<TieredEvChargeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargeCheckpointStore {
    override fun find(code: String): TieredEvChargeCheckpoint? {
        val hit = (cache.get(code) as TieredEvChargeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargeCheckpoint> = inner.list()
}

fun newCachedTieredEvChargeCheckpointStore(inner: TieredEvChargeCheckpointStore, ttl: Int, clock: Clock): TieredEvChargeCheckpointStore {
    return CachedTieredEvChargeCheckpointStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargeCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge checkpoints. Empty fields match all. */
data class TieredEvChargeCheckpointQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargeCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TieredEvChargeCheckpoint>): MutableList<TieredEvChargeCheckpoint> {
        val matched = filter(items) { candidate: TieredEvChargeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargeCheckpoints(store: TieredEvChargeCheckpointStore, query: TieredEvChargeCheckpointQuery): MutableList<TieredEvChargeCheckpoint> {
    return query.apply(store.list())
}
