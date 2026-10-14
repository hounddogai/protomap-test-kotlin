package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection checkpoints by code. */
interface CappedInspectionCheckpointStore {
    fun find(code: String): CappedInspectionCheckpoint?
    fun save(item: CappedInspectionCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionCheckpointStore(
    var items: MutableMap<String, CappedInspectionCheckpoint>,
    var order: MutableList<String>,
) : CappedInspectionCheckpointStore {
    override fun find(code: String): CappedInspectionCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionCheckpoint) {
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

    override fun list(): MutableList<CappedInspectionCheckpoint> {
        val out = mutableListOf<CappedInspectionCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionCheckpointStore(): CappedInspectionCheckpointStore {
    return MemoryCappedInspectionCheckpointStore(
        mutableMapOf<String, CappedInspectionCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionCheckpointStore(
    var inner: CappedInspectionCheckpointStore,
    var cache: TtlCache<CappedInspectionCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionCheckpointStore {
    override fun find(code: String): CappedInspectionCheckpoint? {
        val hit = (cache.get(code) as CappedInspectionCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionCheckpoint> = inner.list()
}

fun newCachedCappedInspectionCheckpointStore(inner: CappedInspectionCheckpointStore, ttl: Int, clock: Clock): CappedInspectionCheckpointStore {
    return CachedCappedInspectionCheckpointStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection checkpoints. Empty fields match all. */
data class CappedInspectionCheckpointQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionCheckpoint): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<CappedInspectionCheckpoint>): MutableList<CappedInspectionCheckpoint> {
        val matched = filter(items) { candidate: CappedInspectionCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionCheckpoints(store: CappedInspectionCheckpointStore, query: CappedInspectionCheckpointQuery): MutableList<CappedInspectionCheckpoint> {
    return query.apply(store.list())
}
