package com.acme.transport.network.zone.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling zone checkpoints by code. */
interface RollingZoneCheckpointStore {
    fun find(code: String): RollingZoneCheckpoint?
    fun save(item: RollingZoneCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingZoneCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingZoneCheckpointStore(
    var items: MutableMap<String, RollingZoneCheckpoint>,
    var order: MutableList<String>,
) : RollingZoneCheckpointStore {
    override fun find(code: String): RollingZoneCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingZoneCheckpoint) {
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

    override fun list(): MutableList<RollingZoneCheckpoint> {
        val out = mutableListOf<RollingZoneCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingZoneCheckpointStore(): RollingZoneCheckpointStore {
    return MemoryRollingZoneCheckpointStore(
        mutableMapOf<String, RollingZoneCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingZoneCheckpointStore(
    var inner: RollingZoneCheckpointStore,
    var cache: TtlCache<RollingZoneCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RollingZoneCheckpointStore {
    override fun find(code: String): RollingZoneCheckpoint? {
        val hit = (cache.get(code) as RollingZoneCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingZoneCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingZoneCheckpoint> = inner.list()
}

fun newCachedRollingZoneCheckpointStore(inner: RollingZoneCheckpointStore, ttl: Int, clock: Clock): RollingZoneCheckpointStore {
    return CachedRollingZoneCheckpointStore(
        inner = inner,
        cache = newTtlCache<RollingZoneCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling zone checkpoints. Empty fields match all. */
data class RollingZoneCheckpointQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingZoneCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingZoneCheckpoint>): MutableList<RollingZoneCheckpoint> {
        val matched = filter(items) { candidate: RollingZoneCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingZoneCheckpoints(store: RollingZoneCheckpointStore, query: RollingZoneCheckpointQuery): MutableList<RollingZoneCheckpoint> {
    return query.apply(store.list())
}
