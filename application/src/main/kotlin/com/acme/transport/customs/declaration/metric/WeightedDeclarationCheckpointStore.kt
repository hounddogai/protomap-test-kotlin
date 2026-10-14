package com.acme.transport.customs.declaration.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted declaration checkpoints by code. */
interface WeightedDeclarationCheckpointStore {
    fun find(code: String): WeightedDeclarationCheckpoint?
    fun save(item: WeightedDeclarationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDeclarationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDeclarationCheckpointStore(
    var items: MutableMap<String, WeightedDeclarationCheckpoint>,
    var order: MutableList<String>,
) : WeightedDeclarationCheckpointStore {
    override fun find(code: String): WeightedDeclarationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDeclarationCheckpoint) {
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

    override fun list(): MutableList<WeightedDeclarationCheckpoint> {
        val out = mutableListOf<WeightedDeclarationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDeclarationCheckpointStore(): WeightedDeclarationCheckpointStore {
    return MemoryWeightedDeclarationCheckpointStore(
        mutableMapOf<String, WeightedDeclarationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDeclarationCheckpointStore(
    var inner: WeightedDeclarationCheckpointStore,
    var cache: TtlCache<WeightedDeclarationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : WeightedDeclarationCheckpointStore {
    override fun find(code: String): WeightedDeclarationCheckpoint? {
        val hit = (cache.get(code) as WeightedDeclarationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDeclarationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDeclarationCheckpoint> = inner.list()
}

fun newCachedWeightedDeclarationCheckpointStore(inner: WeightedDeclarationCheckpointStore, ttl: Int, clock: Clock): WeightedDeclarationCheckpointStore {
    return CachedWeightedDeclarationCheckpointStore(
        inner = inner,
        cache = newTtlCache<WeightedDeclarationCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted declaration checkpoints. Empty fields match all. */
data class WeightedDeclarationCheckpointQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDeclarationCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDeclarationCheckpoint>): MutableList<WeightedDeclarationCheckpoint> {
        val matched = filter(items) { candidate: WeightedDeclarationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDeclarationCheckpoints(store: WeightedDeclarationCheckpointStore, query: WeightedDeclarationCheckpointQuery): MutableList<WeightedDeclarationCheckpoint> {
    return query.apply(store.list())
}
