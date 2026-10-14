package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched declaration snapshots by code. */
interface BatchedDeclarationSnapshotStore {
    fun find(code: String): BatchedDeclarationSnapshot?
    fun save(item: BatchedDeclarationSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDeclarationSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDeclarationSnapshotStore(
    var items: MutableMap<String, BatchedDeclarationSnapshot>,
    var order: MutableList<String>,
) : BatchedDeclarationSnapshotStore {
    override fun find(code: String): BatchedDeclarationSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDeclarationSnapshot) {
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

    override fun list(): MutableList<BatchedDeclarationSnapshot> {
        val out = mutableListOf<BatchedDeclarationSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDeclarationSnapshotStore(): BatchedDeclarationSnapshotStore {
    return MemoryBatchedDeclarationSnapshotStore(
        mutableMapOf<String, BatchedDeclarationSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDeclarationSnapshotStore(
    var inner: BatchedDeclarationSnapshotStore,
    var cache: TtlCache<BatchedDeclarationSnapshot>,
    var hits: Int,
    var misses: Int,
) : BatchedDeclarationSnapshotStore {
    override fun find(code: String): BatchedDeclarationSnapshot? {
        val hit = (cache.get(code) as BatchedDeclarationSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDeclarationSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDeclarationSnapshot> = inner.list()
}

fun newCachedBatchedDeclarationSnapshotStore(inner: BatchedDeclarationSnapshotStore, ttl: Int, clock: Clock): BatchedDeclarationSnapshotStore {
    return CachedBatchedDeclarationSnapshotStore(
        inner = inner,
        cache = newTtlCache<BatchedDeclarationSnapshot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched declaration snapshots. Empty fields match all. */
data class BatchedDeclarationSnapshotQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDeclarationSnapshot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BatchedDeclarationSnapshot>): MutableList<BatchedDeclarationSnapshot> {
        val matched = filter(items) { candidate: BatchedDeclarationSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDeclarationSnapshots(store: BatchedDeclarationSnapshotStore, query: BatchedDeclarationSnapshotQuery): MutableList<BatchedDeclarationSnapshot> {
    return query.apply(store.list())
}
