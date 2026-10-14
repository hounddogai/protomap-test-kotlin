package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return thresholds by code. */
interface LeaseReturnThresholdStore {
    fun find(code: String): LeaseReturnThreshold?
    fun save(item: LeaseReturnThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnThresholdStore(
    var items: MutableMap<String, LeaseReturnThreshold>,
    var order: MutableList<String>,
) : LeaseReturnThresholdStore {
    override fun find(code: String): LeaseReturnThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnThreshold) {
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

    override fun list(): MutableList<LeaseReturnThreshold> {
        val out = mutableListOf<LeaseReturnThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnThresholdStore(): LeaseReturnThresholdStore {
    return MemoryLeaseReturnThresholdStore(
        mutableMapOf<String, LeaseReturnThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnThresholdStore(
    var inner: LeaseReturnThresholdStore,
    var cache: TtlCache<LeaseReturnThreshold>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnThresholdStore {
    override fun find(code: String): LeaseReturnThreshold? {
        val hit = (cache.get(code) as LeaseReturnThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnThreshold> = inner.list()
}

fun newCachedLeaseReturnThresholdStore(inner: LeaseReturnThresholdStore, ttl: Int, clock: Clock): LeaseReturnThresholdStore {
    return CachedLeaseReturnThresholdStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return thresholds. Empty fields match all. */
data class LeaseReturnThresholdQuery(
    var region: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnThreshold>): MutableList<LeaseReturnThreshold> {
        val matched = filter(items) { candidate: LeaseReturnThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnThresholds(store: LeaseReturnThresholdStore, query: LeaseReturnThresholdQuery): MutableList<LeaseReturnThreshold> {
    return query.apply(store.list())
}
