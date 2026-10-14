package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock checkpoints by code. */
interface CrossDockCheckpointStore {
    fun find(code: String): CrossDockCheckpoint?
    fun save(item: CrossDockCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockCheckpointStore(
    var items: MutableMap<String, CrossDockCheckpoint>,
    var order: MutableList<String>,
) : CrossDockCheckpointStore {
    override fun find(code: String): CrossDockCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockCheckpoint) {
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

    override fun list(): MutableList<CrossDockCheckpoint> {
        val out = mutableListOf<CrossDockCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockCheckpointStore(): CrossDockCheckpointStore {
    return MemoryCrossDockCheckpointStore(mutableMapOf<String, CrossDockCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockCheckpointStore(
    var inner: CrossDockCheckpointStore,
    var cache: TtlCache<CrossDockCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CrossDockCheckpointStore {
    override fun find(code: String): CrossDockCheckpoint? {
        val hit = (cache.get(code) as CrossDockCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockCheckpoint> = inner.list()
}

fun newCachedCrossDockCheckpointStore(inner: CrossDockCheckpointStore, ttl: Int, clock: Clock): CrossDockCheckpointStore {
    return CachedCrossDockCheckpointStore(
        inner = inner,
        cache = newTtlCache<CrossDockCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock checkpoints. Empty fields match all. */
data class CrossDockCheckpointQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockCheckpoint>): MutableList<CrossDockCheckpoint> {
        val matched = filter(items) { candidate: CrossDockCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockCheckpoints(store: CrossDockCheckpointStore, query: CrossDockCheckpointQuery): MutableList<CrossDockCheckpoint> {
    return query.apply(store.list())
}
