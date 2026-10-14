package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered transit time batches by code. */
interface TieredTransitTimeBatchStore {
    fun find(code: String): TieredTransitTimeBatch?
    fun save(item: TieredTransitTimeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTransitTimeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTransitTimeBatchStore(
    var items: MutableMap<String, TieredTransitTimeBatch>,
    var order: MutableList<String>,
) : TieredTransitTimeBatchStore {
    override fun find(code: String): TieredTransitTimeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTransitTimeBatch) {
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

    override fun list(): MutableList<TieredTransitTimeBatch> {
        val out = mutableListOf<TieredTransitTimeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTransitTimeBatchStore(): TieredTransitTimeBatchStore {
    return MemoryTieredTransitTimeBatchStore(
        mutableMapOf<String, TieredTransitTimeBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTransitTimeBatchStore(
    var inner: TieredTransitTimeBatchStore,
    var cache: TtlCache<TieredTransitTimeBatch>,
    var hits: Int,
    var misses: Int,
) : TieredTransitTimeBatchStore {
    override fun find(code: String): TieredTransitTimeBatch? {
        val hit = (cache.get(code) as TieredTransitTimeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTransitTimeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTransitTimeBatch> = inner.list()
}

fun newCachedTieredTransitTimeBatchStore(inner: TieredTransitTimeBatchStore, ttl: Int, clock: Clock): TieredTransitTimeBatchStore {
    return CachedTieredTransitTimeBatchStore(
        inner = inner,
        cache = newTtlCache<TieredTransitTimeBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered transit time batches. Empty fields match all. */
data class TieredTransitTimeBatchQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTransitTimeBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTransitTimeBatch>): MutableList<TieredTransitTimeBatch> {
        val matched = filter(items) { candidate: TieredTransitTimeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTransitTimeBatches(store: TieredTransitTimeBatchStore, query: TieredTransitTimeBatchQuery): MutableList<TieredTransitTimeBatch> {
    return query.apply(store.list())
}
