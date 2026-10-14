package com.acme.transport.customs.broker.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending broker checkpoints by code. */
interface PendingBrokerCheckpointStore {
    fun find(code: String): PendingBrokerCheckpoint?
    fun save(item: PendingBrokerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBrokerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBrokerCheckpointStore(
    var items: MutableMap<String, PendingBrokerCheckpoint>,
    var order: MutableList<String>,
) : PendingBrokerCheckpointStore {
    override fun find(code: String): PendingBrokerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBrokerCheckpoint) {
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

    override fun list(): MutableList<PendingBrokerCheckpoint> {
        val out = mutableListOf<PendingBrokerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBrokerCheckpointStore(): PendingBrokerCheckpointStore {
    return MemoryPendingBrokerCheckpointStore(
        mutableMapOf<String, PendingBrokerCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBrokerCheckpointStore(
    var inner: PendingBrokerCheckpointStore,
    var cache: TtlCache<PendingBrokerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PendingBrokerCheckpointStore {
    override fun find(code: String): PendingBrokerCheckpoint? {
        val hit = (cache.get(code) as PendingBrokerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBrokerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBrokerCheckpoint> = inner.list()
}

fun newCachedPendingBrokerCheckpointStore(inner: PendingBrokerCheckpointStore, ttl: Int, clock: Clock): PendingBrokerCheckpointStore {
    return CachedPendingBrokerCheckpointStore(
        inner = inner,
        cache = newTtlCache<PendingBrokerCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending broker checkpoints. Empty fields match all. */
data class PendingBrokerCheckpointQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBrokerCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBrokerCheckpoint>): MutableList<PendingBrokerCheckpoint> {
        val matched = filter(items) { candidate: PendingBrokerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBrokerCheckpoints(store: PendingBrokerCheckpointStore, query: PendingBrokerCheckpointQuery): MutableList<PendingBrokerCheckpoint> {
    return query.apply(store.list())
}
