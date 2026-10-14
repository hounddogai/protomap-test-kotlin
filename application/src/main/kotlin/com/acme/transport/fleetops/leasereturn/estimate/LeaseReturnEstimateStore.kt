package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return estimates by code. */
interface LeaseReturnEstimateStore {
    fun find(code: String): LeaseReturnEstimate?
    fun save(item: LeaseReturnEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnEstimateStore(
    var items: MutableMap<String, LeaseReturnEstimate>,
    var order: MutableList<String>,
) : LeaseReturnEstimateStore {
    override fun find(code: String): LeaseReturnEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnEstimate) {
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

    override fun list(): MutableList<LeaseReturnEstimate> {
        val out = mutableListOf<LeaseReturnEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnEstimateStore(): LeaseReturnEstimateStore {
    return MemoryLeaseReturnEstimateStore(mutableMapOf<String, LeaseReturnEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnEstimateStore(
    var inner: LeaseReturnEstimateStore,
    var cache: TtlCache<LeaseReturnEstimate>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnEstimateStore {
    override fun find(code: String): LeaseReturnEstimate? {
        val hit = (cache.get(code) as LeaseReturnEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnEstimate> = inner.list()
}

fun newCachedLeaseReturnEstimateStore(inner: LeaseReturnEstimateStore, ttl: Int, clock: Clock): LeaseReturnEstimateStore {
    return CachedLeaseReturnEstimateStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return estimates. Empty fields match all. */
data class LeaseReturnEstimateQuery(
    var category: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<LeaseReturnEstimate>): MutableList<LeaseReturnEstimate> {
        val matched = filter(items) { candidate: LeaseReturnEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnEstimates(store: LeaseReturnEstimateStore, query: LeaseReturnEstimateQuery): MutableList<LeaseReturnEstimate> {
    return query.apply(store.list())
}
