package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break checkpoints by code. */
interface RestBreakCheckpointStore {
    fun find(code: String): RestBreakCheckpoint?
    fun save(item: RestBreakCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakCheckpointStore(
    var items: MutableMap<String, RestBreakCheckpoint>,
    var order: MutableList<String>,
) : RestBreakCheckpointStore {
    override fun find(code: String): RestBreakCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakCheckpoint) {
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

    override fun list(): MutableList<RestBreakCheckpoint> {
        val out = mutableListOf<RestBreakCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakCheckpointStore(): RestBreakCheckpointStore {
    return MemoryRestBreakCheckpointStore(mutableMapOf<String, RestBreakCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakCheckpointStore(
    var inner: RestBreakCheckpointStore,
    var cache: TtlCache<RestBreakCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RestBreakCheckpointStore {
    override fun find(code: String): RestBreakCheckpoint? {
        val hit = (cache.get(code) as RestBreakCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakCheckpoint> = inner.list()
}

fun newCachedRestBreakCheckpointStore(inner: RestBreakCheckpointStore, ttl: Int, clock: Clock): RestBreakCheckpointStore {
    return CachedRestBreakCheckpointStore(
        inner = inner,
        cache = newTtlCache<RestBreakCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break checkpoints. Empty fields match all. */
data class RestBreakCheckpointQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakCheckpoint>): MutableList<RestBreakCheckpoint> {
        val matched = filter(items) { candidate: RestBreakCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakCheckpoints(store: RestBreakCheckpointStore, query: RestBreakCheckpointQuery): MutableList<RestBreakCheckpoint> {
    return query.apply(store.list())
}
