package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending transit time checkpoints by code. */
interface PendingTransitTimeCheckpointStore {
    fun find(code: String): PendingTransitTimeCheckpoint?
    fun save(item: PendingTransitTimeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTransitTimeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTransitTimeCheckpointStore(
    var items: MutableMap<String, PendingTransitTimeCheckpoint>,
    var order: MutableList<String>,
) : PendingTransitTimeCheckpointStore {
    override fun find(code: String): PendingTransitTimeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTransitTimeCheckpoint) {
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

    override fun list(): MutableList<PendingTransitTimeCheckpoint> {
        val out = mutableListOf<PendingTransitTimeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTransitTimeCheckpointStore(): PendingTransitTimeCheckpointStore {
    return MemoryPendingTransitTimeCheckpointStore(
        mutableMapOf<String, PendingTransitTimeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTransitTimeCheckpointStore(
    var inner: PendingTransitTimeCheckpointStore,
    var cache: TtlCache<PendingTransitTimeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PendingTransitTimeCheckpointStore {
    override fun find(code: String): PendingTransitTimeCheckpoint? {
        val hit = (cache.get(code) as PendingTransitTimeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTransitTimeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTransitTimeCheckpoint> = inner.list()
}

fun newCachedPendingTransitTimeCheckpointStore(inner: PendingTransitTimeCheckpointStore, ttl: Int, clock: Clock): PendingTransitTimeCheckpointStore {
    return CachedPendingTransitTimeCheckpointStore(
        inner = inner,
        cache = newTtlCache<PendingTransitTimeCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending transit time checkpoints. Empty fields match all. */
data class PendingTransitTimeCheckpointQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTransitTimeCheckpoint): Boolean {
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

    fun apply(items: MutableList<PendingTransitTimeCheckpoint>): MutableList<PendingTransitTimeCheckpoint> {
        val matched = filter(items) { candidate: PendingTransitTimeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTransitTimeCheckpoints(store: PendingTransitTimeCheckpointStore, query: PendingTransitTimeCheckpointQuery): MutableList<PendingTransitTimeCheckpoint> {
    return query.apply(store.list())
}
