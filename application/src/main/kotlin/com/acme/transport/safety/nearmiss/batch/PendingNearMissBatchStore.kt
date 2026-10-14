package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending near miss batches by code. */
interface PendingNearMissBatchStore {
    fun find(code: String): PendingNearMissBatch?
    fun save(item: PendingNearMissBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNearMissBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNearMissBatchStore(
    var items: MutableMap<String, PendingNearMissBatch>,
    var order: MutableList<String>,
) : PendingNearMissBatchStore {
    override fun find(code: String): PendingNearMissBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNearMissBatch) {
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

    override fun list(): MutableList<PendingNearMissBatch> {
        val out = mutableListOf<PendingNearMissBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNearMissBatchStore(): PendingNearMissBatchStore {
    return MemoryPendingNearMissBatchStore(
        mutableMapOf<String, PendingNearMissBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNearMissBatchStore(
    var inner: PendingNearMissBatchStore,
    var cache: TtlCache<PendingNearMissBatch>,
    var hits: Int,
    var misses: Int,
) : PendingNearMissBatchStore {
    override fun find(code: String): PendingNearMissBatch? {
        val hit = (cache.get(code) as PendingNearMissBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNearMissBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNearMissBatch> = inner.list()
}

fun newCachedPendingNearMissBatchStore(inner: PendingNearMissBatchStore, ttl: Int, clock: Clock): PendingNearMissBatchStore {
    return CachedPendingNearMissBatchStore(
        inner = inner,
        cache = newTtlCache<PendingNearMissBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending near miss batches. Empty fields match all. */
data class PendingNearMissBatchQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNearMissBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNearMissBatch>): MutableList<PendingNearMissBatch> {
        val matched = filter(items) { candidate: PendingNearMissBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNearMissBatches(store: PendingNearMissBatchStore, query: PendingNearMissBatchQuery): MutableList<PendingNearMissBatch> {
    return query.apply(store.list())
}
