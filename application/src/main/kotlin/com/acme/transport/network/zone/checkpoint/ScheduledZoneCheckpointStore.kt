package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled zone checkpoints by code. */
interface ScheduledZoneCheckpointStore {
    fun find(code: String): ScheduledZoneCheckpoint?
    fun save(item: ScheduledZoneCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledZoneCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledZoneCheckpointStore(
    var items: MutableMap<String, ScheduledZoneCheckpoint>,
    var order: MutableList<String>,
) : ScheduledZoneCheckpointStore {
    override fun find(code: String): ScheduledZoneCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledZoneCheckpoint) {
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

    override fun list(): MutableList<ScheduledZoneCheckpoint> {
        val out = mutableListOf<ScheduledZoneCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledZoneCheckpointStore(): ScheduledZoneCheckpointStore {
    return MemoryScheduledZoneCheckpointStore(
        mutableMapOf<String, ScheduledZoneCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledZoneCheckpointStore(
    var inner: ScheduledZoneCheckpointStore,
    var cache: TtlCache<ScheduledZoneCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ScheduledZoneCheckpointStore {
    override fun find(code: String): ScheduledZoneCheckpoint? {
        val hit = (cache.get(code) as ScheduledZoneCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledZoneCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledZoneCheckpoint> = inner.list()
}

fun newCachedScheduledZoneCheckpointStore(inner: ScheduledZoneCheckpointStore, ttl: Int, clock: Clock): ScheduledZoneCheckpointStore {
    return CachedScheduledZoneCheckpointStore(
        inner = inner,
        cache = newTtlCache<ScheduledZoneCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled zone checkpoints. Empty fields match all. */
data class ScheduledZoneCheckpointQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledZoneCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledZoneCheckpoint>): MutableList<ScheduledZoneCheckpoint> {
        val matched = filter(items) { candidate: ScheduledZoneCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledZoneCheckpoints(store: ScheduledZoneCheckpointStore, query: ScheduledZoneCheckpointQuery): MutableList<ScheduledZoneCheckpoint> {
    return query.apply(store.list())
}
