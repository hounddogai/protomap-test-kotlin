package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled lease return estimates by code. */
interface ScheduledLeaseReturnEstimateStore {
    fun find(code: String): ScheduledLeaseReturnEstimate?
    fun save(item: ScheduledLeaseReturnEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLeaseReturnEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLeaseReturnEstimateStore(
    var items: MutableMap<String, ScheduledLeaseReturnEstimate>,
    var order: MutableList<String>,
) : ScheduledLeaseReturnEstimateStore {
    override fun find(code: String): ScheduledLeaseReturnEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLeaseReturnEstimate) {
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

    override fun list(): MutableList<ScheduledLeaseReturnEstimate> {
        val out = mutableListOf<ScheduledLeaseReturnEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLeaseReturnEstimateStore(): ScheduledLeaseReturnEstimateStore {
    return MemoryScheduledLeaseReturnEstimateStore(
        mutableMapOf<String, ScheduledLeaseReturnEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLeaseReturnEstimateStore(
    var inner: ScheduledLeaseReturnEstimateStore,
    var cache: TtlCache<ScheduledLeaseReturnEstimate>,
    var hits: Int,
    var misses: Int,
) : ScheduledLeaseReturnEstimateStore {
    override fun find(code: String): ScheduledLeaseReturnEstimate? {
        val hit = (cache.get(code) as ScheduledLeaseReturnEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLeaseReturnEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLeaseReturnEstimate> = inner.list()
}

fun newCachedScheduledLeaseReturnEstimateStore(inner: ScheduledLeaseReturnEstimateStore, ttl: Int, clock: Clock): ScheduledLeaseReturnEstimateStore {
    return CachedScheduledLeaseReturnEstimateStore(
        inner = inner,
        cache = newTtlCache<ScheduledLeaseReturnEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lease return estimates. Empty fields match all. */
data class ScheduledLeaseReturnEstimateQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLeaseReturnEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLeaseReturnEstimate>): MutableList<ScheduledLeaseReturnEstimate> {
        val matched = filter(items) { candidate: ScheduledLeaseReturnEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLeaseReturnEstimates(store: ScheduledLeaseReturnEstimateStore, query: ScheduledLeaseReturnEstimateQuery): MutableList<ScheduledLeaseReturnEstimate> {
    return query.apply(store.list())
}
