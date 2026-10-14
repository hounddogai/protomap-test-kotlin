package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched near miss manifests by code. */
interface BatchedNearMissManifestStore {
    fun find(code: String): BatchedNearMissManifest?
    fun save(item: BatchedNearMissManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNearMissManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNearMissManifestStore(
    var items: MutableMap<String, BatchedNearMissManifest>,
    var order: MutableList<String>,
) : BatchedNearMissManifestStore {
    override fun find(code: String): BatchedNearMissManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNearMissManifest) {
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

    override fun list(): MutableList<BatchedNearMissManifest> {
        val out = mutableListOf<BatchedNearMissManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNearMissManifestStore(): BatchedNearMissManifestStore {
    return MemoryBatchedNearMissManifestStore(
        mutableMapOf<String, BatchedNearMissManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNearMissManifestStore(
    var inner: BatchedNearMissManifestStore,
    var cache: TtlCache<BatchedNearMissManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedNearMissManifestStore {
    override fun find(code: String): BatchedNearMissManifest? {
        val hit = (cache.get(code) as BatchedNearMissManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNearMissManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNearMissManifest> = inner.list()
}

fun newCachedBatchedNearMissManifestStore(inner: BatchedNearMissManifestStore, ttl: Int, clock: Clock): BatchedNearMissManifestStore {
    return CachedBatchedNearMissManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedNearMissManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched near miss manifests. Empty fields match all. */
data class BatchedNearMissManifestQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNearMissManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNearMissManifest>): MutableList<BatchedNearMissManifest> {
        val matched = filter(items) { candidate: BatchedNearMissManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNearMissManifests(store: BatchedNearMissManifestStore, query: BatchedNearMissManifestQuery): MutableList<BatchedNearMissManifest> {
    return query.apply(store.list())
}
