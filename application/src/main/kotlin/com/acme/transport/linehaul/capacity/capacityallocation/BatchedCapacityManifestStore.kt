package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched capacity manifests by code. */
interface BatchedCapacityManifestStore {
    fun find(code: String): BatchedCapacityManifest?
    fun save(item: BatchedCapacityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCapacityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCapacityManifestStore(
    var items: MutableMap<String, BatchedCapacityManifest>,
    var order: MutableList<String>,
) : BatchedCapacityManifestStore {
    override fun find(code: String): BatchedCapacityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCapacityManifest) {
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

    override fun list(): MutableList<BatchedCapacityManifest> {
        val out = mutableListOf<BatchedCapacityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCapacityManifestStore(): BatchedCapacityManifestStore {
    return MemoryBatchedCapacityManifestStore(
        mutableMapOf<String, BatchedCapacityManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCapacityManifestStore(
    var inner: BatchedCapacityManifestStore,
    var cache: TtlCache<BatchedCapacityManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedCapacityManifestStore {
    override fun find(code: String): BatchedCapacityManifest? {
        val hit = (cache.get(code) as BatchedCapacityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCapacityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCapacityManifest> = inner.list()
}

fun newCachedBatchedCapacityManifestStore(inner: BatchedCapacityManifestStore, ttl: Int, clock: Clock): BatchedCapacityManifestStore {
    return CachedBatchedCapacityManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedCapacityManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched capacity manifests. Empty fields match all. */
data class BatchedCapacityManifestQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCapacityManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedCapacityManifest>): MutableList<BatchedCapacityManifest> {
        val matched = filter(items) { candidate: BatchedCapacityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCapacityManifests(store: BatchedCapacityManifestStore, query: BatchedCapacityManifestQuery): MutableList<BatchedCapacityManifest> {
    return query.apply(store.list())
}
