package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset checkpoints by code. */
interface ScheduledRouteOffsetCheckpointStore {
    fun find(code: String): ScheduledRouteOffsetCheckpoint?
    fun save(item: ScheduledRouteOffsetCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetCheckpointStore(
    var items: MutableMap<String, ScheduledRouteOffsetCheckpoint>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetCheckpointStore {
    override fun find(code: String): ScheduledRouteOffsetCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetCheckpoint) {
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

    override fun list(): MutableList<ScheduledRouteOffsetCheckpoint> {
        val out = mutableListOf<ScheduledRouteOffsetCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetCheckpointStore(): ScheduledRouteOffsetCheckpointStore {
    return MemoryScheduledRouteOffsetCheckpointStore(
        mutableMapOf<String, ScheduledRouteOffsetCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetCheckpointStore(
    var inner: ScheduledRouteOffsetCheckpointStore,
    var cache: TtlCache<ScheduledRouteOffsetCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetCheckpointStore {
    override fun find(code: String): ScheduledRouteOffsetCheckpoint? {
        val hit = (cache.get(code) as ScheduledRouteOffsetCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetCheckpoint> = inner.list()
}

fun newCachedScheduledRouteOffsetCheckpointStore(inner: ScheduledRouteOffsetCheckpointStore, ttl: Int, clock: Clock): ScheduledRouteOffsetCheckpointStore {
    return CachedScheduledRouteOffsetCheckpointStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset checkpoints. Empty fields match all. */
data class ScheduledRouteOffsetCheckpointQuery(
    var depot: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetCheckpoint): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRouteOffsetCheckpoint>): MutableList<ScheduledRouteOffsetCheckpoint> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetCheckpoints(store: ScheduledRouteOffsetCheckpointStore, query: ScheduledRouteOffsetCheckpointQuery): MutableList<ScheduledRouteOffsetCheckpoint> {
    return query.apply(store.list())
}
