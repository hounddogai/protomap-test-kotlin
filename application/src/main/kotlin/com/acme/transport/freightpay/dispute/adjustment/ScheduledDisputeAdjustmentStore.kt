package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled dispute adjustments by code. */
interface ScheduledDisputeAdjustmentStore {
    fun find(code: String): ScheduledDisputeAdjustment?
    fun save(item: ScheduledDisputeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDisputeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDisputeAdjustmentStore(
    var items: MutableMap<String, ScheduledDisputeAdjustment>,
    var order: MutableList<String>,
) : ScheduledDisputeAdjustmentStore {
    override fun find(code: String): ScheduledDisputeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDisputeAdjustment) {
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

    override fun list(): MutableList<ScheduledDisputeAdjustment> {
        val out = mutableListOf<ScheduledDisputeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDisputeAdjustmentStore(): ScheduledDisputeAdjustmentStore {
    return MemoryScheduledDisputeAdjustmentStore(
        mutableMapOf<String, ScheduledDisputeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDisputeAdjustmentStore(
    var inner: ScheduledDisputeAdjustmentStore,
    var cache: TtlCache<ScheduledDisputeAdjustment>,
    var hits: Int,
    var misses: Int,
) : ScheduledDisputeAdjustmentStore {
    override fun find(code: String): ScheduledDisputeAdjustment? {
        val hit = (cache.get(code) as ScheduledDisputeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDisputeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDisputeAdjustment> = inner.list()
}

fun newCachedScheduledDisputeAdjustmentStore(inner: ScheduledDisputeAdjustmentStore, ttl: Int, clock: Clock): ScheduledDisputeAdjustmentStore {
    return CachedScheduledDisputeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledDisputeAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dispute adjustments. Empty fields match all. */
data class ScheduledDisputeAdjustmentQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDisputeAdjustment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDisputeAdjustment>): MutableList<ScheduledDisputeAdjustment> {
        val matched = filter(items) { candidate: ScheduledDisputeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDisputeAdjustments(store: ScheduledDisputeAdjustmentStore, query: ScheduledDisputeAdjustmentQuery): MutableList<ScheduledDisputeAdjustment> {
    return query.apply(store.list())
}
