package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient clearance checkpoints by code. */
interface LenientClearanceCheckpointStore {
    fun find(code: String): LenientClearanceCheckpoint?
    fun save(item: LenientClearanceCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientClearanceCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientClearanceCheckpointStore(
    var items: MutableMap<String, LenientClearanceCheckpoint>,
    var order: MutableList<String>,
) : LenientClearanceCheckpointStore {
    override fun find(code: String): LenientClearanceCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientClearanceCheckpoint) {
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

    override fun list(): MutableList<LenientClearanceCheckpoint> {
        val out = mutableListOf<LenientClearanceCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientClearanceCheckpointStore(): LenientClearanceCheckpointStore {
    return MemoryLenientClearanceCheckpointStore(
        mutableMapOf<String, LenientClearanceCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientClearanceCheckpointStore(
    var inner: LenientClearanceCheckpointStore,
    var cache: TtlCache<LenientClearanceCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LenientClearanceCheckpointStore {
    override fun find(code: String): LenientClearanceCheckpoint? {
        val hit = (cache.get(code) as LenientClearanceCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientClearanceCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientClearanceCheckpoint> = inner.list()
}

fun newCachedLenientClearanceCheckpointStore(inner: LenientClearanceCheckpointStore, ttl: Int, clock: Clock): LenientClearanceCheckpointStore {
    return CachedLenientClearanceCheckpointStore(
        inner = inner,
        cache = newTtlCache<LenientClearanceCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient clearance checkpoints. Empty fields match all. */
data class LenientClearanceCheckpointQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientClearanceCheckpoint): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientClearanceCheckpoint>): MutableList<LenientClearanceCheckpoint> {
        val matched = filter(items) { candidate: LenientClearanceCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientClearanceCheckpoints(store: LenientClearanceCheckpointStore, query: LenientClearanceCheckpointQuery): MutableList<LenientClearanceCheckpoint> {
    return query.apply(store.list())
}
