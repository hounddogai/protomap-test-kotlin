package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched defect profiles by code. */
interface BatchedDefectProfileStore {
    fun find(code: String): BatchedDefectProfile?
    fun save(item: BatchedDefectProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDefectProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDefectProfileStore(
    var items: MutableMap<String, BatchedDefectProfile>,
    var order: MutableList<String>,
) : BatchedDefectProfileStore {
    override fun find(code: String): BatchedDefectProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDefectProfile) {
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

    override fun list(): MutableList<BatchedDefectProfile> {
        val out = mutableListOf<BatchedDefectProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDefectProfileStore(): BatchedDefectProfileStore {
    return MemoryBatchedDefectProfileStore(
        mutableMapOf<String, BatchedDefectProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDefectProfileStore(
    var inner: BatchedDefectProfileStore,
    var cache: TtlCache<BatchedDefectProfile>,
    var hits: Int,
    var misses: Int,
) : BatchedDefectProfileStore {
    override fun find(code: String): BatchedDefectProfile? {
        val hit = (cache.get(code) as BatchedDefectProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDefectProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDefectProfile> = inner.list()
}

fun newCachedBatchedDefectProfileStore(inner: BatchedDefectProfileStore, ttl: Int, clock: Clock): BatchedDefectProfileStore {
    return CachedBatchedDefectProfileStore(
        inner = inner,
        cache = newTtlCache<BatchedDefectProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched defect profiles. Empty fields match all. */
data class BatchedDefectProfileQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDefectProfile): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<BatchedDefectProfile>): MutableList<BatchedDefectProfile> {
        val matched = filter(items) { candidate: BatchedDefectProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDefectProfiles(store: BatchedDefectProfileStore, query: BatchedDefectProfileQuery): MutableList<BatchedDefectProfile> {
    return query.apply(store.list())
}
