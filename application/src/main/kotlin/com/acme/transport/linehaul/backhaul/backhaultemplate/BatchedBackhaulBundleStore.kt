package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched backhaul bundles by code. */
interface BatchedBackhaulBundleStore {
    fun find(code: String): BatchedBackhaulBundle?
    fun save(item: BatchedBackhaulBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBackhaulBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBackhaulBundleStore(
    var items: MutableMap<String, BatchedBackhaulBundle>,
    var order: MutableList<String>,
) : BatchedBackhaulBundleStore {
    override fun find(code: String): BatchedBackhaulBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBackhaulBundle) {
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

    override fun list(): MutableList<BatchedBackhaulBundle> {
        val out = mutableListOf<BatchedBackhaulBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBackhaulBundleStore(): BatchedBackhaulBundleStore {
    return MemoryBatchedBackhaulBundleStore(
        mutableMapOf<String, BatchedBackhaulBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBackhaulBundleStore(
    var inner: BatchedBackhaulBundleStore,
    var cache: TtlCache<BatchedBackhaulBundle>,
    var hits: Int,
    var misses: Int,
) : BatchedBackhaulBundleStore {
    override fun find(code: String): BatchedBackhaulBundle? {
        val hit = (cache.get(code) as BatchedBackhaulBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBackhaulBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBackhaulBundle> = inner.list()
}

fun newCachedBatchedBackhaulBundleStore(inner: BatchedBackhaulBundleStore, ttl: Int, clock: Clock): BatchedBackhaulBundleStore {
    return CachedBatchedBackhaulBundleStore(
        inner = inner,
        cache = newTtlCache<BatchedBackhaulBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched backhaul bundles. Empty fields match all. */
data class BatchedBackhaulBundleQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBackhaulBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedBackhaulBundle>): MutableList<BatchedBackhaulBundle> {
        val matched = filter(items) { candidate: BatchedBackhaulBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBackhaulBundles(store: BatchedBackhaulBundleStore, query: BatchedBackhaulBundleQuery): MutableList<BatchedBackhaulBundle> {
    return query.apply(store.list())
}
