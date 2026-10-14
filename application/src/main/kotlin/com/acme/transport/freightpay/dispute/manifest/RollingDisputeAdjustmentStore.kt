package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling dispute adjustments by code. */
interface RollingDisputeAdjustmentStore {
    fun find(code: String): RollingDisputeAdjustment?
    fun save(item: RollingDisputeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDisputeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDisputeAdjustmentStore(
    var items: MutableMap<String, RollingDisputeAdjustment>,
    var order: MutableList<String>,
) : RollingDisputeAdjustmentStore {
    override fun find(code: String): RollingDisputeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDisputeAdjustment) {
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

    override fun list(): MutableList<RollingDisputeAdjustment> {
        val out = mutableListOf<RollingDisputeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDisputeAdjustmentStore(): RollingDisputeAdjustmentStore {
    return MemoryRollingDisputeAdjustmentStore(
        mutableMapOf<String, RollingDisputeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDisputeAdjustmentStore(
    var inner: RollingDisputeAdjustmentStore,
    var cache: TtlCache<RollingDisputeAdjustment>,
    var hits: Int,
    var misses: Int,
) : RollingDisputeAdjustmentStore {
    override fun find(code: String): RollingDisputeAdjustment? {
        val hit = (cache.get(code) as RollingDisputeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDisputeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDisputeAdjustment> = inner.list()
}

fun newCachedRollingDisputeAdjustmentStore(inner: RollingDisputeAdjustmentStore, ttl: Int, clock: Clock): RollingDisputeAdjustmentStore {
    return CachedRollingDisputeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RollingDisputeAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dispute adjustments. Empty fields match all. */
data class RollingDisputeAdjustmentQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDisputeAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDisputeAdjustment>): MutableList<RollingDisputeAdjustment> {
        val matched = filter(items) { candidate: RollingDisputeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDisputeAdjustments(store: RollingDisputeAdjustmentStore, query: RollingDisputeAdjustmentQuery): MutableList<RollingDisputeAdjustment> {
    return query.apply(store.list())
}
