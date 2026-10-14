package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global lease return adjustments by code. */
interface GlobalLeaseReturnAdjustmentStore {
    fun find(code: String): GlobalLeaseReturnAdjustment?
    fun save(item: GlobalLeaseReturnAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLeaseReturnAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLeaseReturnAdjustmentStore(
    var items: MutableMap<String, GlobalLeaseReturnAdjustment>,
    var order: MutableList<String>,
) : GlobalLeaseReturnAdjustmentStore {
    override fun find(code: String): GlobalLeaseReturnAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLeaseReturnAdjustment) {
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

    override fun list(): MutableList<GlobalLeaseReturnAdjustment> {
        val out = mutableListOf<GlobalLeaseReturnAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLeaseReturnAdjustmentStore(): GlobalLeaseReturnAdjustmentStore {
    return MemoryGlobalLeaseReturnAdjustmentStore(
        mutableMapOf<String, GlobalLeaseReturnAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLeaseReturnAdjustmentStore(
    var inner: GlobalLeaseReturnAdjustmentStore,
    var cache: TtlCache<GlobalLeaseReturnAdjustment>,
    var hits: Int,
    var misses: Int,
) : GlobalLeaseReturnAdjustmentStore {
    override fun find(code: String): GlobalLeaseReturnAdjustment? {
        val hit = (cache.get(code) as GlobalLeaseReturnAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLeaseReturnAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLeaseReturnAdjustment> = inner.list()
}

fun newCachedGlobalLeaseReturnAdjustmentStore(inner: GlobalLeaseReturnAdjustmentStore, ttl: Int, clock: Clock): GlobalLeaseReturnAdjustmentStore {
    return CachedGlobalLeaseReturnAdjustmentStore(
        inner = inner,
        cache = newTtlCache<GlobalLeaseReturnAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global lease return adjustments. Empty fields match all. */
data class GlobalLeaseReturnAdjustmentQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLeaseReturnAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLeaseReturnAdjustment>): MutableList<GlobalLeaseReturnAdjustment> {
        val matched = filter(items) { candidate: GlobalLeaseReturnAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLeaseReturnAdjustments(store: GlobalLeaseReturnAdjustmentStore, query: GlobalLeaseReturnAdjustmentQuery): MutableList<GlobalLeaseReturnAdjustment> {
    return query.apply(store.list())
}
