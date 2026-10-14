package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched trailer profiles by code. */
interface BatchedTrailerProfileStore {
    fun find(code: String): BatchedTrailerProfile?
    fun save(item: BatchedTrailerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerProfileStore(
    var items: MutableMap<String, BatchedTrailerProfile>,
    var order: MutableList<String>,
) : BatchedTrailerProfileStore {
    override fun find(code: String): BatchedTrailerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerProfile) {
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

    override fun list(): MutableList<BatchedTrailerProfile> {
        val out = mutableListOf<BatchedTrailerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerProfileStore(): BatchedTrailerProfileStore {
    return MemoryBatchedTrailerProfileStore(
        mutableMapOf<String, BatchedTrailerProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerProfileStore(
    var inner: BatchedTrailerProfileStore,
    var cache: TtlCache<BatchedTrailerProfile>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerProfileStore {
    override fun find(code: String): BatchedTrailerProfile? {
        val hit = (cache.get(code) as BatchedTrailerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerProfile> = inner.list()
}

fun newCachedBatchedTrailerProfileStore(inner: BatchedTrailerProfileStore, ttl: Int, clock: Clock): BatchedTrailerProfileStore {
    return CachedBatchedTrailerProfileStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer profiles. Empty fields match all. */
data class BatchedTrailerProfileQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTrailerProfile>): MutableList<BatchedTrailerProfile> {
        val matched = filter(items) { candidate: BatchedTrailerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerProfiles(store: BatchedTrailerProfileStore, query: BatchedTrailerProfileQuery): MutableList<BatchedTrailerProfile> {
    return query.apply(store.list())
}
