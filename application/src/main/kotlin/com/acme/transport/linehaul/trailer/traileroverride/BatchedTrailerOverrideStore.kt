package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched trailer overrides by code. */
interface BatchedTrailerOverrideStore {
    fun find(code: String): BatchedTrailerOverride?
    fun save(item: BatchedTrailerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerOverrideStore(
    var items: MutableMap<String, BatchedTrailerOverride>,
    var order: MutableList<String>,
) : BatchedTrailerOverrideStore {
    override fun find(code: String): BatchedTrailerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerOverride) {
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

    override fun list(): MutableList<BatchedTrailerOverride> {
        val out = mutableListOf<BatchedTrailerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerOverrideStore(): BatchedTrailerOverrideStore {
    return MemoryBatchedTrailerOverrideStore(
        mutableMapOf<String, BatchedTrailerOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerOverrideStore(
    var inner: BatchedTrailerOverrideStore,
    var cache: TtlCache<BatchedTrailerOverride>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerOverrideStore {
    override fun find(code: String): BatchedTrailerOverride? {
        val hit = (cache.get(code) as BatchedTrailerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerOverride> = inner.list()
}

fun newCachedBatchedTrailerOverrideStore(inner: BatchedTrailerOverrideStore, ttl: Int, clock: Clock): BatchedTrailerOverrideStore {
    return CachedBatchedTrailerOverrideStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer overrides. Empty fields match all. */
data class BatchedTrailerOverrideQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTrailerOverride>): MutableList<BatchedTrailerOverride> {
        val matched = filter(items) { candidate: BatchedTrailerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerOverrides(store: BatchedTrailerOverrideStore, query: BatchedTrailerOverrideQuery): MutableList<BatchedTrailerOverride> {
    return query.apply(store.list())
}
