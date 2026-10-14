package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection checkpoints by code. */
interface RollingInspectionCheckpointStore {
    fun find(code: String): RollingInspectionCheckpoint?
    fun save(item: RollingInspectionCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionCheckpointStore(
    var items: MutableMap<String, RollingInspectionCheckpoint>,
    var order: MutableList<String>,
) : RollingInspectionCheckpointStore {
    override fun find(code: String): RollingInspectionCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionCheckpoint) {
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

    override fun list(): MutableList<RollingInspectionCheckpoint> {
        val out = mutableListOf<RollingInspectionCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionCheckpointStore(): RollingInspectionCheckpointStore {
    return MemoryRollingInspectionCheckpointStore(
        mutableMapOf<String, RollingInspectionCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionCheckpointStore(
    var inner: RollingInspectionCheckpointStore,
    var cache: TtlCache<RollingInspectionCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionCheckpointStore {
    override fun find(code: String): RollingInspectionCheckpoint? {
        val hit = (cache.get(code) as RollingInspectionCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionCheckpoint> = inner.list()
}

fun newCachedRollingInspectionCheckpointStore(inner: RollingInspectionCheckpointStore, ttl: Int, clock: Clock): RollingInspectionCheckpointStore {
    return CachedRollingInspectionCheckpointStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection checkpoints. Empty fields match all. */
data class RollingInspectionCheckpointQuery(
    var unit: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionCheckpoint): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionCheckpoint>): MutableList<RollingInspectionCheckpoint> {
        val matched = filter(items) { candidate: RollingInspectionCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionCheckpoints(store: RollingInspectionCheckpointStore, query: RollingInspectionCheckpointQuery): MutableList<RollingInspectionCheckpoint> {
    return query.apply(store.list())
}
