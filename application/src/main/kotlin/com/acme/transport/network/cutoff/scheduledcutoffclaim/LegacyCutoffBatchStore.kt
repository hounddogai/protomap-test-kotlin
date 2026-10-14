package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy cutoff batches by code. */
interface LegacyCutoffBatchStore {
    fun find(code: String): LegacyCutoffBatch?
    fun save(item: LegacyCutoffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCutoffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCutoffBatchStore(
    var items: MutableMap<String, LegacyCutoffBatch>,
    var order: MutableList<String>,
) : LegacyCutoffBatchStore {
    override fun find(code: String): LegacyCutoffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCutoffBatch) {
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

    override fun list(): MutableList<LegacyCutoffBatch> {
        val out = mutableListOf<LegacyCutoffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCutoffBatchStore(): LegacyCutoffBatchStore {
    return MemoryLegacyCutoffBatchStore(mutableMapOf<String, LegacyCutoffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCutoffBatchStore(
    var inner: LegacyCutoffBatchStore,
    var cache: TtlCache<LegacyCutoffBatch>,
    var hits: Int,
    var misses: Int,
) : LegacyCutoffBatchStore {
    override fun find(code: String): LegacyCutoffBatch? {
        val hit = (cache.get(code) as LegacyCutoffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCutoffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCutoffBatch> = inner.list()
}

fun newCachedLegacyCutoffBatchStore(inner: LegacyCutoffBatchStore, ttl: Int, clock: Clock): LegacyCutoffBatchStore {
    return CachedLegacyCutoffBatchStore(
        inner = inner,
        cache = newTtlCache<LegacyCutoffBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cutoff batches. Empty fields match all. */
data class LegacyCutoffBatchQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCutoffBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCutoffBatch>): MutableList<LegacyCutoffBatch> {
        val matched = filter(items) { candidate: LegacyCutoffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCutoffBatches(store: LegacyCutoffBatchStore, query: LegacyCutoffBatchQuery): MutableList<LegacyCutoffBatch> {
    return query.apply(store.list())
}
