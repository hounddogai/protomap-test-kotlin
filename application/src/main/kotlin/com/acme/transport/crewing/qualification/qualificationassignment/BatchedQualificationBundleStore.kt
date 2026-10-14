package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification bundles by code. */
interface BatchedQualificationBundleStore {
    fun find(code: String): BatchedQualificationBundle?
    fun save(item: BatchedQualificationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationBundleStore(
    var items: MutableMap<String, BatchedQualificationBundle>,
    var order: MutableList<String>,
) : BatchedQualificationBundleStore {
    override fun find(code: String): BatchedQualificationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationBundle) {
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

    override fun list(): MutableList<BatchedQualificationBundle> {
        val out = mutableListOf<BatchedQualificationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationBundleStore(): BatchedQualificationBundleStore {
    return MemoryBatchedQualificationBundleStore(
        mutableMapOf<String, BatchedQualificationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationBundleStore(
    var inner: BatchedQualificationBundleStore,
    var cache: TtlCache<BatchedQualificationBundle>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationBundleStore {
    override fun find(code: String): BatchedQualificationBundle? {
        val hit = (cache.get(code) as BatchedQualificationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationBundle> = inner.list()
}

fun newCachedBatchedQualificationBundleStore(inner: BatchedQualificationBundleStore, ttl: Int, clock: Clock): BatchedQualificationBundleStore {
    return CachedBatchedQualificationBundleStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification bundles. Empty fields match all. */
data class BatchedQualificationBundleQuery(
    var region: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationBundle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationBundle>): MutableList<BatchedQualificationBundle> {
        val matched = filter(items) { candidate: BatchedQualificationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationBundles(store: BatchedQualificationBundleStore, query: BatchedQualificationBundleQuery): MutableList<BatchedQualificationBundle> {
    return query.apply(store.list())
}
