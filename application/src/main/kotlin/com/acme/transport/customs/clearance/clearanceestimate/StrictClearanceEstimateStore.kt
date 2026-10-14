package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict clearance estimates by code. */
interface StrictClearanceEstimateStore {
    fun find(code: String): StrictClearanceEstimate?
    fun save(item: StrictClearanceEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictClearanceEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictClearanceEstimateStore(
    var items: MutableMap<String, StrictClearanceEstimate>,
    var order: MutableList<String>,
) : StrictClearanceEstimateStore {
    override fun find(code: String): StrictClearanceEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictClearanceEstimate) {
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

    override fun list(): MutableList<StrictClearanceEstimate> {
        val out = mutableListOf<StrictClearanceEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictClearanceEstimateStore(): StrictClearanceEstimateStore {
    return MemoryStrictClearanceEstimateStore(
        mutableMapOf<String, StrictClearanceEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictClearanceEstimateStore(
    var inner: StrictClearanceEstimateStore,
    var cache: TtlCache<StrictClearanceEstimate>,
    var hits: Int,
    var misses: Int,
) : StrictClearanceEstimateStore {
    override fun find(code: String): StrictClearanceEstimate? {
        val hit = (cache.get(code) as StrictClearanceEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictClearanceEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictClearanceEstimate> = inner.list()
}

fun newCachedStrictClearanceEstimateStore(inner: StrictClearanceEstimateStore, ttl: Int, clock: Clock): StrictClearanceEstimateStore {
    return CachedStrictClearanceEstimateStore(
        inner = inner,
        cache = newTtlCache<StrictClearanceEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict clearance estimates. Empty fields match all. */
data class StrictClearanceEstimateQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictClearanceEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictClearanceEstimate>): MutableList<StrictClearanceEstimate> {
        val matched = filter(items) { candidate: StrictClearanceEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictClearanceEstimates(store: StrictClearanceEstimateStore, query: StrictClearanceEstimateQuery): MutableList<StrictClearanceEstimate> {
    return query.apply(store.list())
}
