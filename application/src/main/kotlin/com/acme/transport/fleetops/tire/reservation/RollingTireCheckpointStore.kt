package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tire checkpoints by code. */
interface RollingTireCheckpointStore {
    fun find(code: String): RollingTireCheckpoint?
    fun save(item: RollingTireCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTireCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTireCheckpointStore(
    var items: MutableMap<String, RollingTireCheckpoint>,
    var order: MutableList<String>,
) : RollingTireCheckpointStore {
    override fun find(code: String): RollingTireCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTireCheckpoint) {
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

    override fun list(): MutableList<RollingTireCheckpoint> {
        val out = mutableListOf<RollingTireCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTireCheckpointStore(): RollingTireCheckpointStore {
    return MemoryRollingTireCheckpointStore(
        mutableMapOf<String, RollingTireCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTireCheckpointStore(
    var inner: RollingTireCheckpointStore,
    var cache: TtlCache<RollingTireCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RollingTireCheckpointStore {
    override fun find(code: String): RollingTireCheckpoint? {
        val hit = (cache.get(code) as RollingTireCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTireCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTireCheckpoint> = inner.list()
}

fun newCachedRollingTireCheckpointStore(inner: RollingTireCheckpointStore, ttl: Int, clock: Clock): RollingTireCheckpointStore {
    return CachedRollingTireCheckpointStore(
        inner = inner,
        cache = newTtlCache<RollingTireCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tire checkpoints. Empty fields match all. */
data class RollingTireCheckpointQuery(
    var region: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTireCheckpoint): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTireCheckpoint>): MutableList<RollingTireCheckpoint> {
        val matched = filter(items) { candidate: RollingTireCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTireCheckpoints(store: RollingTireCheckpointStore, query: RollingTireCheckpointQuery): MutableList<RollingTireCheckpoint> {
    return query.apply(store.list())
}
