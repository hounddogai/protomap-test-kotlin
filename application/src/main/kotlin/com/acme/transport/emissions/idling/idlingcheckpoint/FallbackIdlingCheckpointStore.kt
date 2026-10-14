package com.acme.transport.emissions.idling.idlingcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback idling checkpoints by code. */
interface FallbackIdlingCheckpointStore {
    fun find(code: String): FallbackIdlingCheckpoint?
    fun save(item: FallbackIdlingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIdlingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIdlingCheckpointStore(
    var items: MutableMap<String, FallbackIdlingCheckpoint>,
    var order: MutableList<String>,
) : FallbackIdlingCheckpointStore {
    override fun find(code: String): FallbackIdlingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIdlingCheckpoint) {
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

    override fun list(): MutableList<FallbackIdlingCheckpoint> {
        val out = mutableListOf<FallbackIdlingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIdlingCheckpointStore(): FallbackIdlingCheckpointStore {
    return MemoryFallbackIdlingCheckpointStore(
        mutableMapOf<String, FallbackIdlingCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIdlingCheckpointStore(
    var inner: FallbackIdlingCheckpointStore,
    var cache: TtlCache<FallbackIdlingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FallbackIdlingCheckpointStore {
    override fun find(code: String): FallbackIdlingCheckpoint? {
        val hit = (cache.get(code) as FallbackIdlingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIdlingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIdlingCheckpoint> = inner.list()
}

fun newCachedFallbackIdlingCheckpointStore(inner: FallbackIdlingCheckpointStore, ttl: Int, clock: Clock): FallbackIdlingCheckpointStore {
    return CachedFallbackIdlingCheckpointStore(
        inner = inner,
        cache = newTtlCache<FallbackIdlingCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback idling checkpoints. Empty fields match all. */
data class FallbackIdlingCheckpointQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIdlingCheckpoint): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<FallbackIdlingCheckpoint>): MutableList<FallbackIdlingCheckpoint> {
        val matched = filter(items) { candidate: FallbackIdlingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIdlingCheckpoints(store: FallbackIdlingCheckpointStore, query: FallbackIdlingCheckpointQuery): MutableList<FallbackIdlingCheckpoint> {
    return query.apply(store.list())
}
