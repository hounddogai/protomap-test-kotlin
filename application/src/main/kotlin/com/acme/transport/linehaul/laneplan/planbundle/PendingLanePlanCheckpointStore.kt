package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending lane plan checkpoints by code. */
interface PendingLanePlanCheckpointStore {
    fun find(code: String): PendingLanePlanCheckpoint?
    fun save(item: PendingLanePlanCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLanePlanCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLanePlanCheckpointStore(
    var items: MutableMap<String, PendingLanePlanCheckpoint>,
    var order: MutableList<String>,
) : PendingLanePlanCheckpointStore {
    override fun find(code: String): PendingLanePlanCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLanePlanCheckpoint) {
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

    override fun list(): MutableList<PendingLanePlanCheckpoint> {
        val out = mutableListOf<PendingLanePlanCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLanePlanCheckpointStore(): PendingLanePlanCheckpointStore {
    return MemoryPendingLanePlanCheckpointStore(
        mutableMapOf<String, PendingLanePlanCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLanePlanCheckpointStore(
    var inner: PendingLanePlanCheckpointStore,
    var cache: TtlCache<PendingLanePlanCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PendingLanePlanCheckpointStore {
    override fun find(code: String): PendingLanePlanCheckpoint? {
        val hit = (cache.get(code) as PendingLanePlanCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLanePlanCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLanePlanCheckpoint> = inner.list()
}

fun newCachedPendingLanePlanCheckpointStore(inner: PendingLanePlanCheckpointStore, ttl: Int, clock: Clock): PendingLanePlanCheckpointStore {
    return CachedPendingLanePlanCheckpointStore(
        inner = inner,
        cache = newTtlCache<PendingLanePlanCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending lane plan checkpoints. Empty fields match all. */
data class PendingLanePlanCheckpointQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLanePlanCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLanePlanCheckpoint>): MutableList<PendingLanePlanCheckpoint> {
        val matched = filter(items) { candidate: PendingLanePlanCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLanePlanCheckpoints(store: PendingLanePlanCheckpointStore, query: PendingLanePlanCheckpointQuery): MutableList<PendingLanePlanCheckpoint> {
    return query.apply(store.list())
}
