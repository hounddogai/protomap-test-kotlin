package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return adjustments by code. */
interface LeaseReturnAdjustmentStore {
    fun find(code: String): LeaseReturnAdjustment?
    fun save(item: LeaseReturnAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnAdjustmentStore(
    var items: MutableMap<String, LeaseReturnAdjustment>,
    var order: MutableList<String>,
) : LeaseReturnAdjustmentStore {
    override fun find(code: String): LeaseReturnAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnAdjustment) {
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

    override fun list(): MutableList<LeaseReturnAdjustment> {
        val out = mutableListOf<LeaseReturnAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnAdjustmentStore(): LeaseReturnAdjustmentStore {
    return MemoryLeaseReturnAdjustmentStore(
        mutableMapOf<String, LeaseReturnAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnAdjustmentStore(
    var inner: LeaseReturnAdjustmentStore,
    var cache: TtlCache<LeaseReturnAdjustment>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnAdjustmentStore {
    override fun find(code: String): LeaseReturnAdjustment? {
        val hit = (cache.get(code) as LeaseReturnAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnAdjustment> = inner.list()
}

fun newCachedLeaseReturnAdjustmentStore(inner: LeaseReturnAdjustmentStore, ttl: Int, clock: Clock): LeaseReturnAdjustmentStore {
    return CachedLeaseReturnAdjustmentStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return adjustments. Empty fields match all. */
data class LeaseReturnAdjustmentQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnAdjustment>): MutableList<LeaseReturnAdjustment> {
        val matched = filter(items) { candidate: LeaseReturnAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnAdjustments(store: LeaseReturnAdjustmentStore, query: LeaseReturnAdjustmentQuery): MutableList<LeaseReturnAdjustment> {
    return query.apply(store.list())
}
