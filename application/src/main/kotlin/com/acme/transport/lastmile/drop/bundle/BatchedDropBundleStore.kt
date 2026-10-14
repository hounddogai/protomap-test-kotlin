package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched drop bundles by code. */
interface BatchedDropBundleStore {
    fun find(code: String): BatchedDropBundle?
    fun save(item: BatchedDropBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDropBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDropBundleStore(
    var items: MutableMap<String, BatchedDropBundle>,
    var order: MutableList<String>,
) : BatchedDropBundleStore {
    override fun find(code: String): BatchedDropBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDropBundle) {
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

    override fun list(): MutableList<BatchedDropBundle> {
        val out = mutableListOf<BatchedDropBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDropBundleStore(): BatchedDropBundleStore {
    return MemoryBatchedDropBundleStore(mutableMapOf<String, BatchedDropBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDropBundleStore(
    var inner: BatchedDropBundleStore,
    var cache: TtlCache<BatchedDropBundle>,
    var hits: Int,
    var misses: Int,
) : BatchedDropBundleStore {
    override fun find(code: String): BatchedDropBundle? {
        val hit = (cache.get(code) as BatchedDropBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDropBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDropBundle> = inner.list()
}

fun newCachedBatchedDropBundleStore(inner: BatchedDropBundleStore, ttl: Int, clock: Clock): BatchedDropBundleStore {
    return CachedBatchedDropBundleStore(
        inner = inner,
        cache = newTtlCache<BatchedDropBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched drop bundles. Empty fields match all. */
data class BatchedDropBundleQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDropBundle): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDropBundle>): MutableList<BatchedDropBundle> {
        val matched = filter(items) { candidate: BatchedDropBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDropBundles(store: BatchedDropBundleStore, query: BatchedDropBundleQuery): MutableList<BatchedDropBundle> {
    return query.apply(store.list())
}
