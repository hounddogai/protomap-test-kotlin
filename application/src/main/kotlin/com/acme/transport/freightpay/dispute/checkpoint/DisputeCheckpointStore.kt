package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute checkpoints by code. */
interface DisputeCheckpointStore {
    fun find(code: String): DisputeCheckpoint?
    fun save(item: DisputeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeCheckpointStore(
    var items: MutableMap<String, DisputeCheckpoint>,
    var order: MutableList<String>,
) : DisputeCheckpointStore {
    override fun find(code: String): DisputeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeCheckpoint) {
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

    override fun list(): MutableList<DisputeCheckpoint> {
        val out = mutableListOf<DisputeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeCheckpointStore(): DisputeCheckpointStore {
    return MemoryDisputeCheckpointStore(mutableMapOf<String, DisputeCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeCheckpointStore(
    var inner: DisputeCheckpointStore,
    var cache: TtlCache<DisputeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DisputeCheckpointStore {
    override fun find(code: String): DisputeCheckpoint? {
        val hit = (cache.get(code) as DisputeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeCheckpoint> = inner.list()
}

fun newCachedDisputeCheckpointStore(inner: DisputeCheckpointStore, ttl: Int, clock: Clock): DisputeCheckpointStore {
    return CachedDisputeCheckpointStore(
        inner = inner,
        cache = newTtlCache<DisputeCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute checkpoints. Empty fields match all. */
data class DisputeCheckpointQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeCheckpoint>): MutableList<DisputeCheckpoint> {
        val matched = filter(items) { candidate: DisputeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeCheckpoints(store: DisputeCheckpointStore, query: DisputeCheckpointQuery): MutableList<DisputeCheckpoint> {
    return query.apply(store.list())
}
