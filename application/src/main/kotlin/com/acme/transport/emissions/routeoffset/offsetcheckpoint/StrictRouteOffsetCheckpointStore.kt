package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict route offset checkpoints by code. */
interface StrictRouteOffsetCheckpointStore {
    fun find(code: String): StrictRouteOffsetCheckpoint?
    fun save(item: StrictRouteOffsetCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRouteOffsetCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRouteOffsetCheckpointStore(
    var items: MutableMap<String, StrictRouteOffsetCheckpoint>,
    var order: MutableList<String>,
) : StrictRouteOffsetCheckpointStore {
    override fun find(code: String): StrictRouteOffsetCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRouteOffsetCheckpoint) {
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

    override fun list(): MutableList<StrictRouteOffsetCheckpoint> {
        val out = mutableListOf<StrictRouteOffsetCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRouteOffsetCheckpointStore(): StrictRouteOffsetCheckpointStore {
    return MemoryStrictRouteOffsetCheckpointStore(
        mutableMapOf<String, StrictRouteOffsetCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRouteOffsetCheckpointStore(
    var inner: StrictRouteOffsetCheckpointStore,
    var cache: TtlCache<StrictRouteOffsetCheckpoint>,
    var hits: Int,
    var misses: Int,
) : StrictRouteOffsetCheckpointStore {
    override fun find(code: String): StrictRouteOffsetCheckpoint? {
        val hit = (cache.get(code) as StrictRouteOffsetCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRouteOffsetCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRouteOffsetCheckpoint> = inner.list()
}

fun newCachedStrictRouteOffsetCheckpointStore(inner: StrictRouteOffsetCheckpointStore, ttl: Int, clock: Clock): StrictRouteOffsetCheckpointStore {
    return CachedStrictRouteOffsetCheckpointStore(
        inner = inner,
        cache = newTtlCache<StrictRouteOffsetCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict route offset checkpoints. Empty fields match all. */
data class StrictRouteOffsetCheckpointQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRouteOffsetCheckpoint): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRouteOffsetCheckpoint>): MutableList<StrictRouteOffsetCheckpoint> {
        val matched = filter(items) { candidate: StrictRouteOffsetCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRouteOffsetCheckpoints(store: StrictRouteOffsetCheckpointStore, query: StrictRouteOffsetCheckpointQuery): MutableList<StrictRouteOffsetCheckpoint> {
    return query.apply(store.list())
}
