package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict trailer yard checkpoints by code. */
interface StrictTrailerYardCheckpointStore {
    fun find(code: String): StrictTrailerYardCheckpoint?
    fun save(item: StrictTrailerYardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTrailerYardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTrailerYardCheckpointStore(
    var items: MutableMap<String, StrictTrailerYardCheckpoint>,
    var order: MutableList<String>,
) : StrictTrailerYardCheckpointStore {
    override fun find(code: String): StrictTrailerYardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTrailerYardCheckpoint) {
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

    override fun list(): MutableList<StrictTrailerYardCheckpoint> {
        val out = mutableListOf<StrictTrailerYardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTrailerYardCheckpointStore(): StrictTrailerYardCheckpointStore {
    return MemoryStrictTrailerYardCheckpointStore(
        mutableMapOf<String, StrictTrailerYardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTrailerYardCheckpointStore(
    var inner: StrictTrailerYardCheckpointStore,
    var cache: TtlCache<StrictTrailerYardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : StrictTrailerYardCheckpointStore {
    override fun find(code: String): StrictTrailerYardCheckpoint? {
        val hit = (cache.get(code) as StrictTrailerYardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTrailerYardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTrailerYardCheckpoint> = inner.list()
}

fun newCachedStrictTrailerYardCheckpointStore(inner: StrictTrailerYardCheckpointStore, ttl: Int, clock: Clock): StrictTrailerYardCheckpointStore {
    return CachedStrictTrailerYardCheckpointStore(
        inner = inner,
        cache = newTtlCache<StrictTrailerYardCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict trailer yard checkpoints. Empty fields match all. */
data class StrictTrailerYardCheckpointQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTrailerYardCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTrailerYardCheckpoint>): MutableList<StrictTrailerYardCheckpoint> {
        val matched = filter(items) { candidate: StrictTrailerYardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTrailerYardCheckpoints(store: StrictTrailerYardCheckpointStore, query: StrictTrailerYardCheckpointQuery): MutableList<StrictTrailerYardCheckpoint> {
    return query.apply(store.list())
}
