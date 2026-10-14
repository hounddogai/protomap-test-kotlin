package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback dispute checkpoints by code. */
interface FallbackDisputeCheckpointStore {
    fun find(code: String): FallbackDisputeCheckpoint?
    fun save(item: FallbackDisputeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDisputeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDisputeCheckpointStore(
    var items: MutableMap<String, FallbackDisputeCheckpoint>,
    var order: MutableList<String>,
) : FallbackDisputeCheckpointStore {
    override fun find(code: String): FallbackDisputeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDisputeCheckpoint) {
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

    override fun list(): MutableList<FallbackDisputeCheckpoint> {
        val out = mutableListOf<FallbackDisputeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDisputeCheckpointStore(): FallbackDisputeCheckpointStore {
    return MemoryFallbackDisputeCheckpointStore(
        mutableMapOf<String, FallbackDisputeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDisputeCheckpointStore(
    var inner: FallbackDisputeCheckpointStore,
    var cache: TtlCache<FallbackDisputeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FallbackDisputeCheckpointStore {
    override fun find(code: String): FallbackDisputeCheckpoint? {
        val hit = (cache.get(code) as FallbackDisputeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDisputeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDisputeCheckpoint> = inner.list()
}

fun newCachedFallbackDisputeCheckpointStore(inner: FallbackDisputeCheckpointStore, ttl: Int, clock: Clock): FallbackDisputeCheckpointStore {
    return CachedFallbackDisputeCheckpointStore(
        inner = inner,
        cache = newTtlCache<FallbackDisputeCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dispute checkpoints. Empty fields match all. */
data class FallbackDisputeCheckpointQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDisputeCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDisputeCheckpoint>): MutableList<FallbackDisputeCheckpoint> {
        val matched = filter(items) { candidate: FallbackDisputeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDisputeCheckpoints(store: FallbackDisputeCheckpointStore, query: FallbackDisputeCheckpointQuery): MutableList<FallbackDisputeCheckpoint> {
    return query.apply(store.list())
}
