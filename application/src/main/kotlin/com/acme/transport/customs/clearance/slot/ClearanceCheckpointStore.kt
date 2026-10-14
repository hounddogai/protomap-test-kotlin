package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance checkpoints by code. */
interface ClearanceCheckpointStore {
    fun find(code: String): ClearanceCheckpoint?
    fun save(item: ClearanceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceCheckpointStore(
    var items: MutableMap<String, ClearanceCheckpoint>,
    var order: MutableList<String>,
) : ClearanceCheckpointStore {
    override fun find(code: String): ClearanceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceCheckpoint) {
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

    override fun list(): MutableList<ClearanceCheckpoint> {
        val out = mutableListOf<ClearanceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceCheckpointStore(): ClearanceCheckpointStore {
    return MemoryClearanceCheckpointStore(mutableMapOf<String, ClearanceCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceCheckpointStore(
    var inner: ClearanceCheckpointStore,
    var cache: TtlCache<ClearanceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ClearanceCheckpointStore {
    override fun find(code: String): ClearanceCheckpoint? {
        val hit = (cache.get(code) as ClearanceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceCheckpoint> = inner.list()
}

fun newCachedClearanceCheckpointStore(inner: ClearanceCheckpointStore, ttl: Int, clock: Clock): ClearanceCheckpointStore {
    return CachedClearanceCheckpointStore(
        inner = inner,
        cache = newTtlCache<ClearanceCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance checkpoints. Empty fields match all. */
data class ClearanceCheckpointQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceCheckpoint>): MutableList<ClearanceCheckpoint> {
        val matched = filter(items) { candidate: ClearanceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceCheckpoints(store: ClearanceCheckpointStore, query: ClearanceCheckpointQuery): MutableList<ClearanceCheckpoint> {
    return query.apply(store.list())
}
