package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy clearance checkpoints by code. */
interface LegacyClearanceCheckpointStore {
    fun find(code: String): LegacyClearanceCheckpoint?
    fun save(item: LegacyClearanceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyClearanceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyClearanceCheckpointStore(
    var items: MutableMap<String, LegacyClearanceCheckpoint>,
    var order: MutableList<String>,
) : LegacyClearanceCheckpointStore {
    override fun find(code: String): LegacyClearanceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyClearanceCheckpoint) {
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

    override fun list(): MutableList<LegacyClearanceCheckpoint> {
        val out = mutableListOf<LegacyClearanceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyClearanceCheckpointStore(): LegacyClearanceCheckpointStore {
    return MemoryLegacyClearanceCheckpointStore(
        mutableMapOf<String, LegacyClearanceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyClearanceCheckpointStore(
    var inner: LegacyClearanceCheckpointStore,
    var cache: TtlCache<LegacyClearanceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LegacyClearanceCheckpointStore {
    override fun find(code: String): LegacyClearanceCheckpoint? {
        val hit = (cache.get(code) as LegacyClearanceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyClearanceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyClearanceCheckpoint> = inner.list()
}

fun newCachedLegacyClearanceCheckpointStore(inner: LegacyClearanceCheckpointStore, ttl: Int, clock: Clock): LegacyClearanceCheckpointStore {
    return CachedLegacyClearanceCheckpointStore(
        inner = inner,
        cache = newTtlCache<LegacyClearanceCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy clearance checkpoints. Empty fields match all. */
data class LegacyClearanceCheckpointQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyClearanceCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyClearanceCheckpoint>): MutableList<LegacyClearanceCheckpoint> {
        val matched = filter(items) { candidate: LegacyClearanceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyClearanceCheckpoints(store: LegacyClearanceCheckpointStore, query: LegacyClearanceCheckpointQuery): MutableList<LegacyClearanceCheckpoint> {
    return query.apply(store.list())
}
