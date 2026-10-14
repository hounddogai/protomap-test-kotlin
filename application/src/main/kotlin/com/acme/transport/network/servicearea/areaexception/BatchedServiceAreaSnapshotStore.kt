package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched service area snapshots by code. */
interface BatchedServiceAreaSnapshotStore {
    fun find(code: String): BatchedServiceAreaSnapshot?
    fun save(item: BatchedServiceAreaSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServiceAreaSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServiceAreaSnapshotStore(
    var items: MutableMap<String, BatchedServiceAreaSnapshot>,
    var order: MutableList<String>,
) : BatchedServiceAreaSnapshotStore {
    override fun find(code: String): BatchedServiceAreaSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServiceAreaSnapshot) {
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

    override fun list(): MutableList<BatchedServiceAreaSnapshot> {
        val out = mutableListOf<BatchedServiceAreaSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServiceAreaSnapshotStore(): BatchedServiceAreaSnapshotStore {
    return MemoryBatchedServiceAreaSnapshotStore(
        mutableMapOf<String, BatchedServiceAreaSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServiceAreaSnapshotStore(
    var inner: BatchedServiceAreaSnapshotStore,
    var cache: TtlCache<BatchedServiceAreaSnapshot>,
    var hits: Int,
    var misses: Int,
) : BatchedServiceAreaSnapshotStore {
    override fun find(code: String): BatchedServiceAreaSnapshot? {
        val hit = (cache.get(code) as BatchedServiceAreaSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServiceAreaSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServiceAreaSnapshot> = inner.list()
}

fun newCachedBatchedServiceAreaSnapshotStore(inner: BatchedServiceAreaSnapshotStore, ttl: Int, clock: Clock): BatchedServiceAreaSnapshotStore {
    return CachedBatchedServiceAreaSnapshotStore(
        inner = inner,
        cache = newTtlCache<BatchedServiceAreaSnapshot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service area snapshots. Empty fields match all. */
data class BatchedServiceAreaSnapshotQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServiceAreaSnapshot): Boolean {
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

    fun apply(items: MutableList<BatchedServiceAreaSnapshot>): MutableList<BatchedServiceAreaSnapshot> {
        val matched = filter(items) { candidate: BatchedServiceAreaSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServiceAreaSnapshots(store: BatchedServiceAreaSnapshotStore, query: BatchedServiceAreaSnapshotQuery): MutableList<BatchedServiceAreaSnapshot> {
    return query.apply(store.list())
}
