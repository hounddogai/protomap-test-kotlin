package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door checkpoints by code. */
interface DockDoorCheckpointStore {
    fun find(code: String): DockDoorCheckpoint?
    fun save(item: DockDoorCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorCheckpointStore(
    var items: MutableMap<String, DockDoorCheckpoint>,
    var order: MutableList<String>,
) : DockDoorCheckpointStore {
    override fun find(code: String): DockDoorCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorCheckpoint) {
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

    override fun list(): MutableList<DockDoorCheckpoint> {
        val out = mutableListOf<DockDoorCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorCheckpointStore(): DockDoorCheckpointStore {
    return MemoryDockDoorCheckpointStore(mutableMapOf<String, DockDoorCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorCheckpointStore(
    var inner: DockDoorCheckpointStore,
    var cache: TtlCache<DockDoorCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DockDoorCheckpointStore {
    override fun find(code: String): DockDoorCheckpoint? {
        val hit = (cache.get(code) as DockDoorCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorCheckpoint> = inner.list()
}

fun newCachedDockDoorCheckpointStore(inner: DockDoorCheckpointStore, ttl: Int, clock: Clock): DockDoorCheckpointStore {
    return CachedDockDoorCheckpointStore(
        inner = inner,
        cache = newTtlCache<DockDoorCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door checkpoints. Empty fields match all. */
data class DockDoorCheckpointQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorCheckpoint>): MutableList<DockDoorCheckpoint> {
        val matched = filter(items) { candidate: DockDoorCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorCheckpoints(store: DockDoorCheckpointStore, query: DockDoorCheckpointQuery): MutableList<DockDoorCheckpoint> {
    return query.apply(store.list())
}
