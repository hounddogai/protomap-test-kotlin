package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan checkpoints by code. */
interface FallbackLanePlanCheckpointStore {
    fun find(code: String): FallbackLanePlanCheckpoint?
    fun save(item: FallbackLanePlanCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanCheckpointStore(
    var items: MutableMap<String, FallbackLanePlanCheckpoint>,
    var order: MutableList<String>,
) : FallbackLanePlanCheckpointStore {
    override fun find(code: String): FallbackLanePlanCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanCheckpoint) {
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

    override fun list(): MutableList<FallbackLanePlanCheckpoint> {
        val out = mutableListOf<FallbackLanePlanCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanCheckpointStore(): FallbackLanePlanCheckpointStore {
    return MemoryFallbackLanePlanCheckpointStore(
        mutableMapOf<String, FallbackLanePlanCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanCheckpointStore(
    var inner: FallbackLanePlanCheckpointStore,
    var cache: TtlCache<FallbackLanePlanCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanCheckpointStore {
    override fun find(code: String): FallbackLanePlanCheckpoint? {
        val hit = (cache.get(code) as FallbackLanePlanCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanCheckpoint> = inner.list()
}

fun newCachedFallbackLanePlanCheckpointStore(inner: FallbackLanePlanCheckpointStore, ttl: Int, clock: Clock): FallbackLanePlanCheckpointStore {
    return CachedFallbackLanePlanCheckpointStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan checkpoints. Empty fields match all. */
data class FallbackLanePlanCheckpointQuery(
    var unit: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanCheckpoint): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanCheckpoint>): MutableList<FallbackLanePlanCheckpoint> {
        val matched = filter(items) { candidate: FallbackLanePlanCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanCheckpoints(store: FallbackLanePlanCheckpointStore, query: FallbackLanePlanCheckpointQuery): MutableList<FallbackLanePlanCheckpoint> {
    return query.apply(store.list())
}
