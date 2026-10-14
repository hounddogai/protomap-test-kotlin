package com.acme.transport.network.node.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy node batches by code. */
interface LegacyNodeBatchStore {
    fun find(code: String): LegacyNodeBatch?
    fun save(item: LegacyNodeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyNodeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyNodeBatchStore(
    var items: MutableMap<String, LegacyNodeBatch>,
    var order: MutableList<String>,
) : LegacyNodeBatchStore {
    override fun find(code: String): LegacyNodeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyNodeBatch) {
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

    override fun list(): MutableList<LegacyNodeBatch> {
        val out = mutableListOf<LegacyNodeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyNodeBatchStore(): LegacyNodeBatchStore {
    return MemoryLegacyNodeBatchStore(mutableMapOf<String, LegacyNodeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyNodeBatchStore(
    var inner: LegacyNodeBatchStore,
    var cache: TtlCache<LegacyNodeBatch>,
    var hits: Int,
    var misses: Int,
) : LegacyNodeBatchStore {
    override fun find(code: String): LegacyNodeBatch? {
        val hit = (cache.get(code) as LegacyNodeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyNodeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyNodeBatch> = inner.list()
}

fun newCachedLegacyNodeBatchStore(inner: LegacyNodeBatchStore, ttl: Int, clock: Clock): LegacyNodeBatchStore {
    return CachedLegacyNodeBatchStore(
        inner = inner,
        cache = newTtlCache<LegacyNodeBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy node batches. Empty fields match all. */
data class LegacyNodeBatchQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyNodeBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyNodeBatch>): MutableList<LegacyNodeBatch> {
        val matched = filter(items) { candidate: LegacyNodeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyNodeBatches(store: LegacyNodeBatchStore, query: LegacyNodeBatchQuery): MutableList<LegacyNodeBatch> {
    return query.apply(store.list())
}
