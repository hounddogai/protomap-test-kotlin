package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual hold estimates by code. */
interface ManualHoldEstimateStore {
    fun find(code: String): ManualHoldEstimate?
    fun save(item: ManualHoldEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHoldEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHoldEstimateStore(
    var items: MutableMap<String, ManualHoldEstimate>,
    var order: MutableList<String>,
) : ManualHoldEstimateStore {
    override fun find(code: String): ManualHoldEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHoldEstimate) {
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

    override fun list(): MutableList<ManualHoldEstimate> {
        val out = mutableListOf<ManualHoldEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHoldEstimateStore(): ManualHoldEstimateStore {
    return MemoryManualHoldEstimateStore(mutableMapOf<String, ManualHoldEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHoldEstimateStore(
    var inner: ManualHoldEstimateStore,
    var cache: TtlCache<ManualHoldEstimate>,
    var hits: Int,
    var misses: Int,
) : ManualHoldEstimateStore {
    override fun find(code: String): ManualHoldEstimate? {
        val hit = (cache.get(code) as ManualHoldEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHoldEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHoldEstimate> = inner.list()
}

fun newCachedManualHoldEstimateStore(inner: ManualHoldEstimateStore, ttl: Int, clock: Clock): ManualHoldEstimateStore {
    return CachedManualHoldEstimateStore(
        inner = inner,
        cache = newTtlCache<ManualHoldEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hold estimates. Empty fields match all. */
data class ManualHoldEstimateQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHoldEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHoldEstimate>): MutableList<ManualHoldEstimate> {
        val matched = filter(items) { candidate: ManualHoldEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHoldEstimates(store: ManualHoldEstimateStore, query: ManualHoldEstimateQuery): MutableList<ManualHoldEstimate> {
    return query.apply(store.list())
}
