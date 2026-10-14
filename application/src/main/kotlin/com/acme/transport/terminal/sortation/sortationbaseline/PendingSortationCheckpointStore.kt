package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending sortation checkpoints by code. */
interface PendingSortationCheckpointStore {
    fun find(code: String): PendingSortationCheckpoint?
    fun save(item: PendingSortationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSortationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSortationCheckpointStore(
    var items: MutableMap<String, PendingSortationCheckpoint>,
    var order: MutableList<String>,
) : PendingSortationCheckpointStore {
    override fun find(code: String): PendingSortationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSortationCheckpoint) {
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

    override fun list(): MutableList<PendingSortationCheckpoint> {
        val out = mutableListOf<PendingSortationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSortationCheckpointStore(): PendingSortationCheckpointStore {
    return MemoryPendingSortationCheckpointStore(
        mutableMapOf<String, PendingSortationCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSortationCheckpointStore(
    var inner: PendingSortationCheckpointStore,
    var cache: TtlCache<PendingSortationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : PendingSortationCheckpointStore {
    override fun find(code: String): PendingSortationCheckpoint? {
        val hit = (cache.get(code) as PendingSortationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSortationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSortationCheckpoint> = inner.list()
}

fun newCachedPendingSortationCheckpointStore(inner: PendingSortationCheckpointStore, ttl: Int, clock: Clock): PendingSortationCheckpointStore {
    return CachedPendingSortationCheckpointStore(
        inner = inner,
        cache = newTtlCache<PendingSortationCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending sortation checkpoints. Empty fields match all. */
data class PendingSortationCheckpointQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSortationCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSortationCheckpoint>): MutableList<PendingSortationCheckpoint> {
        val matched = filter(items) { candidate: PendingSortationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSortationCheckpoints(store: PendingSortationCheckpointStore, query: PendingSortationCheckpointQuery): MutableList<PendingSortationCheckpoint> {
    return query.apply(store.list())
}
