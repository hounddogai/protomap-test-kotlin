package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient route offset checkpoints by code. */
interface LenientRouteOffsetCheckpointStore {
    fun find(code: String): LenientRouteOffsetCheckpoint?
    fun save(item: LenientRouteOffsetCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRouteOffsetCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRouteOffsetCheckpointStore(
    var items: MutableMap<String, LenientRouteOffsetCheckpoint>,
    var order: MutableList<String>,
) : LenientRouteOffsetCheckpointStore {
    override fun find(code: String): LenientRouteOffsetCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRouteOffsetCheckpoint) {
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

    override fun list(): MutableList<LenientRouteOffsetCheckpoint> {
        val out = mutableListOf<LenientRouteOffsetCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRouteOffsetCheckpointStore(): LenientRouteOffsetCheckpointStore {
    return MemoryLenientRouteOffsetCheckpointStore(
        mutableMapOf<String, LenientRouteOffsetCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRouteOffsetCheckpointStore(
    var inner: LenientRouteOffsetCheckpointStore,
    var cache: TtlCache<LenientRouteOffsetCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LenientRouteOffsetCheckpointStore {
    override fun find(code: String): LenientRouteOffsetCheckpoint? {
        val hit = (cache.get(code) as LenientRouteOffsetCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRouteOffsetCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRouteOffsetCheckpoint> = inner.list()
}

fun newCachedLenientRouteOffsetCheckpointStore(inner: LenientRouteOffsetCheckpointStore, ttl: Int, clock: Clock): LenientRouteOffsetCheckpointStore {
    return CachedLenientRouteOffsetCheckpointStore(
        inner = inner,
        cache = newTtlCache<LenientRouteOffsetCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient route offset checkpoints. Empty fields match all. */
data class LenientRouteOffsetCheckpointQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRouteOffsetCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRouteOffsetCheckpoint>): MutableList<LenientRouteOffsetCheckpoint> {
        val matched = filter(items) { candidate: LenientRouteOffsetCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRouteOffsetCheckpoints(store: LenientRouteOffsetCheckpointStore, query: LenientRouteOffsetCheckpointQuery): MutableList<LenientRouteOffsetCheckpoint> {
    return query.apply(store.list())
}
