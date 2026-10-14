package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return tiers by code. */
interface LeaseReturnTierStore {
    fun find(code: String): LeaseReturnTier?
    fun save(item: LeaseReturnTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnTierStore(
    var items: MutableMap<String, LeaseReturnTier>,
    var order: MutableList<String>,
) : LeaseReturnTierStore {
    override fun find(code: String): LeaseReturnTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnTier) {
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

    override fun list(): MutableList<LeaseReturnTier> {
        val out = mutableListOf<LeaseReturnTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnTierStore(): LeaseReturnTierStore {
    return MemoryLeaseReturnTierStore(mutableMapOf<String, LeaseReturnTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnTierStore(
    var inner: LeaseReturnTierStore,
    var cache: TtlCache<LeaseReturnTier>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnTierStore {
    override fun find(code: String): LeaseReturnTier? {
        val hit = (cache.get(code) as LeaseReturnTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnTier> = inner.list()
}

fun newCachedLeaseReturnTierStore(inner: LeaseReturnTierStore, ttl: Int, clock: Clock): LeaseReturnTierStore {
    return CachedLeaseReturnTierStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return tiers. Empty fields match all. */
data class LeaseReturnTierQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<LeaseReturnTier>): MutableList<LeaseReturnTier> {
        val matched = filter(items) { candidate: LeaseReturnTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnTiers(store: LeaseReturnTierStore, query: LeaseReturnTierQuery): MutableList<LeaseReturnTier> {
    return query.apply(store.list())
}
