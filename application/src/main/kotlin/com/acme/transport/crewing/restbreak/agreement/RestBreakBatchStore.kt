package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break batches by code. */
interface RestBreakBatchStore {
    fun find(code: String): RestBreakBatch?
    fun save(item: RestBreakBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakBatchStore(
    var items: MutableMap<String, RestBreakBatch>,
    var order: MutableList<String>,
) : RestBreakBatchStore {
    override fun find(code: String): RestBreakBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakBatch) {
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

    override fun list(): MutableList<RestBreakBatch> {
        val out = mutableListOf<RestBreakBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakBatchStore(): RestBreakBatchStore {
    return MemoryRestBreakBatchStore(mutableMapOf<String, RestBreakBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakBatchStore(
    var inner: RestBreakBatchStore,
    var cache: TtlCache<RestBreakBatch>,
    var hits: Int,
    var misses: Int,
) : RestBreakBatchStore {
    override fun find(code: String): RestBreakBatch? {
        val hit = (cache.get(code) as RestBreakBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakBatch> = inner.list()
}

fun newCachedRestBreakBatchStore(inner: RestBreakBatchStore, ttl: Int, clock: Clock): RestBreakBatchStore {
    return CachedRestBreakBatchStore(
        inner = inner,
        cache = newTtlCache<RestBreakBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break batches. Empty fields match all. */
data class RestBreakBatchQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakBatch): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RestBreakBatch>): MutableList<RestBreakBatch> {
        val matched = filter(items) { candidate: RestBreakBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakBatches(store: RestBreakBatchStore, query: RestBreakBatchQuery): MutableList<RestBreakBatch> {
    return query.apply(store.list())
}
