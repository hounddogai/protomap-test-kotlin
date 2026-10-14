package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization checkpoints by code. */
interface UtilizationCheckpointStore {
    fun find(code: String): UtilizationCheckpoint?
    fun save(item: UtilizationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationCheckpointStore(
    var items: MutableMap<String, UtilizationCheckpoint>,
    var order: MutableList<String>,
) : UtilizationCheckpointStore {
    override fun find(code: String): UtilizationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationCheckpoint) {
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

    override fun list(): MutableList<UtilizationCheckpoint> {
        val out = mutableListOf<UtilizationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationCheckpointStore(): UtilizationCheckpointStore {
    return MemoryUtilizationCheckpointStore(
        mutableMapOf<String, UtilizationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationCheckpointStore(
    var inner: UtilizationCheckpointStore,
    var cache: TtlCache<UtilizationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : UtilizationCheckpointStore {
    override fun find(code: String): UtilizationCheckpoint? {
        val hit = (cache.get(code) as UtilizationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationCheckpoint> = inner.list()
}

fun newCachedUtilizationCheckpointStore(inner: UtilizationCheckpointStore, ttl: Int, clock: Clock): UtilizationCheckpointStore {
    return CachedUtilizationCheckpointStore(
        inner = inner,
        cache = newTtlCache<UtilizationCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization checkpoints. Empty fields match all. */
data class UtilizationCheckpointQuery(
    var lane: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationCheckpoint): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationCheckpoint>): MutableList<UtilizationCheckpoint> {
        val matched = filter(items) { candidate: UtilizationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationCheckpoints(store: UtilizationCheckpointStore, query: UtilizationCheckpointQuery): MutableList<UtilizationCheckpoint> {
    return query.apply(store.list())
}
