package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection checkpoints by code. */
interface PendingInspectionCheckpointStore {
    fun find(code: String): PendingInspectionCheckpoint?
    fun save(item: PendingInspectionCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionCheckpointStore(
    var items: MutableMap<String, PendingInspectionCheckpoint>,
    var order: MutableList<String>,
) : PendingInspectionCheckpointStore {
    override fun find(code: String): PendingInspectionCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionCheckpoint) {
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

    override fun list(): MutableList<PendingInspectionCheckpoint> {
        val out = mutableListOf<PendingInspectionCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionCheckpointStore(): PendingInspectionCheckpointStore {
    return MemoryPendingInspectionCheckpointStore(
        mutableMapOf<String, PendingInspectionCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionCheckpointStore(
    var inner: PendingInspectionCheckpointStore,
    var cache: TtlCache<PendingInspectionCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionCheckpointStore {
    override fun find(code: String): PendingInspectionCheckpoint? {
        val hit = (cache.get(code) as PendingInspectionCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionCheckpoint> = inner.list()
}

fun newCachedPendingInspectionCheckpointStore(inner: PendingInspectionCheckpointStore, ttl: Int, clock: Clock): PendingInspectionCheckpointStore {
    return CachedPendingInspectionCheckpointStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection checkpoints. Empty fields match all. */
data class PendingInspectionCheckpointQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionCheckpoint>): MutableList<PendingInspectionCheckpoint> {
        val matched = filter(items) { candidate: PendingInspectionCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionCheckpoints(store: PendingInspectionCheckpointStore, query: PendingInspectionCheckpointQuery): MutableList<PendingInspectionCheckpoint> {
    return query.apply(store.list())
}
