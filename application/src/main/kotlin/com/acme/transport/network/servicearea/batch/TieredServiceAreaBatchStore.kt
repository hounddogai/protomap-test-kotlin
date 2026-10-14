package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered service area batches by code. */
interface TieredServiceAreaBatchStore {
    fun find(code: String): TieredServiceAreaBatch?
    fun save(item: TieredServiceAreaBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredServiceAreaBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredServiceAreaBatchStore(
    var items: MutableMap<String, TieredServiceAreaBatch>,
    var order: MutableList<String>,
) : TieredServiceAreaBatchStore {
    override fun find(code: String): TieredServiceAreaBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredServiceAreaBatch) {
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

    override fun list(): MutableList<TieredServiceAreaBatch> {
        val out = mutableListOf<TieredServiceAreaBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredServiceAreaBatchStore(): TieredServiceAreaBatchStore {
    return MemoryTieredServiceAreaBatchStore(
        mutableMapOf<String, TieredServiceAreaBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredServiceAreaBatchStore(
    var inner: TieredServiceAreaBatchStore,
    var cache: TtlCache<TieredServiceAreaBatch>,
    var hits: Int,
    var misses: Int,
) : TieredServiceAreaBatchStore {
    override fun find(code: String): TieredServiceAreaBatch? {
        val hit = (cache.get(code) as TieredServiceAreaBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredServiceAreaBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredServiceAreaBatch> = inner.list()
}

fun newCachedTieredServiceAreaBatchStore(inner: TieredServiceAreaBatchStore, ttl: Int, clock: Clock): TieredServiceAreaBatchStore {
    return CachedTieredServiceAreaBatchStore(
        inner = inner,
        cache = newTtlCache<TieredServiceAreaBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered service area batches. Empty fields match all. */
data class TieredServiceAreaBatchQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredServiceAreaBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TieredServiceAreaBatch>): MutableList<TieredServiceAreaBatch> {
        val matched = filter(items) { candidate: TieredServiceAreaBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredServiceAreaBatches(store: TieredServiceAreaBatchStore, query: TieredServiceAreaBatchQuery): MutableList<TieredServiceAreaBatch> {
    return query.apply(store.list())
}
