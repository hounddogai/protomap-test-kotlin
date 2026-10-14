package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard checkpoints by code. */
interface TrailerYardCheckpointStore {
    fun find(code: String): TrailerYardCheckpoint?
    fun save(item: TrailerYardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardCheckpointStore(
    var items: MutableMap<String, TrailerYardCheckpoint>,
    var order: MutableList<String>,
) : TrailerYardCheckpointStore {
    override fun find(code: String): TrailerYardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardCheckpoint) {
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

    override fun list(): MutableList<TrailerYardCheckpoint> {
        val out = mutableListOf<TrailerYardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardCheckpointStore(): TrailerYardCheckpointStore {
    return MemoryTrailerYardCheckpointStore(
        mutableMapOf<String, TrailerYardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardCheckpointStore(
    var inner: TrailerYardCheckpointStore,
    var cache: TtlCache<TrailerYardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TrailerYardCheckpointStore {
    override fun find(code: String): TrailerYardCheckpoint? {
        val hit = (cache.get(code) as TrailerYardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardCheckpoint> = inner.list()
}

fun newCachedTrailerYardCheckpointStore(inner: TrailerYardCheckpointStore, ttl: Int, clock: Clock): TrailerYardCheckpointStore {
    return CachedTrailerYardCheckpointStore(
        inner = inner,
        cache = newTtlCache<TrailerYardCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard checkpoints. Empty fields match all. */
data class TrailerYardCheckpointQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardCheckpoint>): MutableList<TrailerYardCheckpoint> {
        val matched = filter(items) { candidate: TrailerYardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardCheckpoints(store: TrailerYardCheckpointStore, query: TrailerYardCheckpointQuery): MutableList<TrailerYardCheckpoint> {
    return query.apply(store.list())
}
