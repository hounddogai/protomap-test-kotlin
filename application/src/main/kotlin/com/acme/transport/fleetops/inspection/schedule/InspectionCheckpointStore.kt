package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection checkpoints by code. */
interface InspectionCheckpointStore {
    fun find(code: String): InspectionCheckpoint?
    fun save(item: InspectionCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionCheckpointStore(
    var items: MutableMap<String, InspectionCheckpoint>,
    var order: MutableList<String>,
) : InspectionCheckpointStore {
    override fun find(code: String): InspectionCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionCheckpoint) {
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

    override fun list(): MutableList<InspectionCheckpoint> {
        val out = mutableListOf<InspectionCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionCheckpointStore(): InspectionCheckpointStore {
    return MemoryInspectionCheckpointStore(
        mutableMapOf<String, InspectionCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionCheckpointStore(
    var inner: InspectionCheckpointStore,
    var cache: TtlCache<InspectionCheckpoint>,
    var hits: Int,
    var misses: Int,
) : InspectionCheckpointStore {
    override fun find(code: String): InspectionCheckpoint? {
        val hit = (cache.get(code) as InspectionCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionCheckpoint> = inner.list()
}

fun newCachedInspectionCheckpointStore(inner: InspectionCheckpointStore, ttl: Int, clock: Clock): InspectionCheckpointStore {
    return CachedInspectionCheckpointStore(
        inner = inner,
        cache = newTtlCache<InspectionCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection checkpoints. Empty fields match all. */
data class InspectionCheckpointQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionCheckpoint>): MutableList<InspectionCheckpoint> {
        val matched = filter(items) { candidate: InspectionCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionCheckpoints(store: InspectionCheckpointStore, query: InspectionCheckpointQuery): MutableList<InspectionCheckpoint> {
    return query.apply(store.list())
}
