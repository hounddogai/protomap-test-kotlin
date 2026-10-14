package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered swap checkpoints by code. */
interface TieredSwapCheckpointStore {
    fun find(code: String): TieredSwapCheckpoint?
    fun save(item: TieredSwapCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSwapCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSwapCheckpointStore(
    var items: MutableMap<String, TieredSwapCheckpoint>,
    var order: MutableList<String>,
) : TieredSwapCheckpointStore {
    override fun find(code: String): TieredSwapCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSwapCheckpoint) {
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

    override fun list(): MutableList<TieredSwapCheckpoint> {
        val out = mutableListOf<TieredSwapCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSwapCheckpointStore(): TieredSwapCheckpointStore {
    return MemoryTieredSwapCheckpointStore(
        mutableMapOf<String, TieredSwapCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSwapCheckpointStore(
    var inner: TieredSwapCheckpointStore,
    var cache: TtlCache<TieredSwapCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TieredSwapCheckpointStore {
    override fun find(code: String): TieredSwapCheckpoint? {
        val hit = (cache.get(code) as TieredSwapCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSwapCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSwapCheckpoint> = inner.list()
}

fun newCachedTieredSwapCheckpointStore(inner: TieredSwapCheckpointStore, ttl: Int, clock: Clock): TieredSwapCheckpointStore {
    return CachedTieredSwapCheckpointStore(
        inner = inner,
        cache = newTtlCache<TieredSwapCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered swap checkpoints. Empty fields match all. */
data class TieredSwapCheckpointQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSwapCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredSwapCheckpoint>): MutableList<TieredSwapCheckpoint> {
        val matched = filter(items) { candidate: TieredSwapCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSwapCheckpoints(store: TieredSwapCheckpointStore, query: TieredSwapCheckpointQuery): MutableList<TieredSwapCheckpoint> {
    return query.apply(store.list())
}
