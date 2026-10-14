package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled hub checkpoints by code. */
interface ScheduledHubCheckpointStore {
    fun find(code: String): ScheduledHubCheckpoint?
    fun save(item: ScheduledHubCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHubCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHubCheckpointStore(
    var items: MutableMap<String, ScheduledHubCheckpoint>,
    var order: MutableList<String>,
) : ScheduledHubCheckpointStore {
    override fun find(code: String): ScheduledHubCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHubCheckpoint) {
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

    override fun list(): MutableList<ScheduledHubCheckpoint> {
        val out = mutableListOf<ScheduledHubCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHubCheckpointStore(): ScheduledHubCheckpointStore {
    return MemoryScheduledHubCheckpointStore(
        mutableMapOf<String, ScheduledHubCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHubCheckpointStore(
    var inner: ScheduledHubCheckpointStore,
    var cache: TtlCache<ScheduledHubCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ScheduledHubCheckpointStore {
    override fun find(code: String): ScheduledHubCheckpoint? {
        val hit = (cache.get(code) as ScheduledHubCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHubCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHubCheckpoint> = inner.list()
}

fun newCachedScheduledHubCheckpointStore(inner: ScheduledHubCheckpointStore, ttl: Int, clock: Clock): ScheduledHubCheckpointStore {
    return CachedScheduledHubCheckpointStore(
        inner = inner,
        cache = newTtlCache<ScheduledHubCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hub checkpoints. Empty fields match all. */
data class ScheduledHubCheckpointQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHubCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHubCheckpoint>): MutableList<ScheduledHubCheckpoint> {
        val matched = filter(items) { candidate: ScheduledHubCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHubCheckpoints(store: ScheduledHubCheckpointStore, query: ScheduledHubCheckpointQuery): MutableList<ScheduledHubCheckpoint> {
    return query.apply(store.list())
}
