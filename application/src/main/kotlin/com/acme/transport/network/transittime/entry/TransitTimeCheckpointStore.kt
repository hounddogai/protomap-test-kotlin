package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time checkpoints by code. */
interface TransitTimeCheckpointStore {
    fun find(code: String): TransitTimeCheckpoint?
    fun save(item: TransitTimeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeCheckpointStore(
    var items: MutableMap<String, TransitTimeCheckpoint>,
    var order: MutableList<String>,
) : TransitTimeCheckpointStore {
    override fun find(code: String): TransitTimeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeCheckpoint) {
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

    override fun list(): MutableList<TransitTimeCheckpoint> {
        val out = mutableListOf<TransitTimeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeCheckpointStore(): TransitTimeCheckpointStore {
    return MemoryTransitTimeCheckpointStore(
        mutableMapOf<String, TransitTimeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeCheckpointStore(
    var inner: TransitTimeCheckpointStore,
    var cache: TtlCache<TransitTimeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TransitTimeCheckpointStore {
    override fun find(code: String): TransitTimeCheckpoint? {
        val hit = (cache.get(code) as TransitTimeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeCheckpoint> = inner.list()
}

fun newCachedTransitTimeCheckpointStore(inner: TransitTimeCheckpointStore, ttl: Int, clock: Clock): TransitTimeCheckpointStore {
    return CachedTransitTimeCheckpointStore(
        inner = inner,
        cache = newTtlCache<TransitTimeCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time checkpoints. Empty fields match all. */
data class TransitTimeCheckpointQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeCheckpoint>): MutableList<TransitTimeCheckpoint> {
        val matched = filter(items) { candidate: TransitTimeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeCheckpoints(store: TransitTimeCheckpointStore, query: TransitTimeCheckpointQuery): MutableList<TransitTimeCheckpoint> {
    return query.apply(store.list())
}
