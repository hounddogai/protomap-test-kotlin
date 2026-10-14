package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery checkpoints by code. */
interface RedeliveryCheckpointStore {
    fun find(code: String): RedeliveryCheckpoint?
    fun save(item: RedeliveryCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryCheckpointStore(
    var items: MutableMap<String, RedeliveryCheckpoint>,
    var order: MutableList<String>,
) : RedeliveryCheckpointStore {
    override fun find(code: String): RedeliveryCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryCheckpoint) {
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

    override fun list(): MutableList<RedeliveryCheckpoint> {
        val out = mutableListOf<RedeliveryCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryCheckpointStore(): RedeliveryCheckpointStore {
    return MemoryRedeliveryCheckpointStore(
        mutableMapOf<String, RedeliveryCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryCheckpointStore(
    var inner: RedeliveryCheckpointStore,
    var cache: TtlCache<RedeliveryCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RedeliveryCheckpointStore {
    override fun find(code: String): RedeliveryCheckpoint? {
        val hit = (cache.get(code) as RedeliveryCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryCheckpoint> = inner.list()
}

fun newCachedRedeliveryCheckpointStore(inner: RedeliveryCheckpointStore, ttl: Int, clock: Clock): RedeliveryCheckpointStore {
    return CachedRedeliveryCheckpointStore(
        inner = inner,
        cache = newTtlCache<RedeliveryCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery checkpoints. Empty fields match all. */
data class RedeliveryCheckpointQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryCheckpoint>): MutableList<RedeliveryCheckpoint> {
        val matched = filter(items) { candidate: RedeliveryCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryCheckpoints(store: RedeliveryCheckpointStore, query: RedeliveryCheckpointQuery): MutableList<RedeliveryCheckpoint> {
    return query.apply(store.list())
}
