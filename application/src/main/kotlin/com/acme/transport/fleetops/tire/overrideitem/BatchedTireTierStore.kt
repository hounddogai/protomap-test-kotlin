package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tire tiers by code. */
interface BatchedTireTierStore {
    fun find(code: String): BatchedTireTier?
    fun save(item: BatchedTireTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTireTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTireTierStore(
    var items: MutableMap<String, BatchedTireTier>,
    var order: MutableList<String>,
) : BatchedTireTierStore {
    override fun find(code: String): BatchedTireTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTireTier) {
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

    override fun list(): MutableList<BatchedTireTier> {
        val out = mutableListOf<BatchedTireTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTireTierStore(): BatchedTireTierStore {
    return MemoryBatchedTireTierStore(mutableMapOf<String, BatchedTireTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTireTierStore(
    var inner: BatchedTireTierStore,
    var cache: TtlCache<BatchedTireTier>,
    var hits: Int,
    var misses: Int,
) : BatchedTireTierStore {
    override fun find(code: String): BatchedTireTier? {
        val hit = (cache.get(code) as BatchedTireTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTireTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTireTier> = inner.list()
}

fun newCachedBatchedTireTierStore(inner: BatchedTireTierStore, ttl: Int, clock: Clock): BatchedTireTierStore {
    return CachedBatchedTireTierStore(
        inner = inner,
        cache = newTtlCache<BatchedTireTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tire tiers. Empty fields match all. */
data class BatchedTireTierQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTireTier): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTireTier>): MutableList<BatchedTireTier> {
        val matched = filter(items) { candidate: BatchedTireTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTireTiers(store: BatchedTireTierStore, query: BatchedTireTierQuery): MutableList<BatchedTireTier> {
    return query.apply(store.list())
}
