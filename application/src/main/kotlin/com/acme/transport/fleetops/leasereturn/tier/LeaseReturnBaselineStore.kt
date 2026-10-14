package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return baselines by code. */
interface LeaseReturnBaselineStore {
    fun find(code: String): LeaseReturnBaseline?
    fun save(item: LeaseReturnBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnBaselineStore(
    var items: MutableMap<String, LeaseReturnBaseline>,
    var order: MutableList<String>,
) : LeaseReturnBaselineStore {
    override fun find(code: String): LeaseReturnBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnBaseline) {
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

    override fun list(): MutableList<LeaseReturnBaseline> {
        val out = mutableListOf<LeaseReturnBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnBaselineStore(): LeaseReturnBaselineStore {
    return MemoryLeaseReturnBaselineStore(mutableMapOf<String, LeaseReturnBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnBaselineStore(
    var inner: LeaseReturnBaselineStore,
    var cache: TtlCache<LeaseReturnBaseline>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnBaselineStore {
    override fun find(code: String): LeaseReturnBaseline? {
        val hit = (cache.get(code) as LeaseReturnBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnBaseline> = inner.list()
}

fun newCachedLeaseReturnBaselineStore(inner: LeaseReturnBaselineStore, ttl: Int, clock: Clock): LeaseReturnBaselineStore {
    return CachedLeaseReturnBaselineStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return baselines. Empty fields match all. */
data class LeaseReturnBaselineQuery(
    var country: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnBaseline>): MutableList<LeaseReturnBaseline> {
        val matched = filter(items) { candidate: LeaseReturnBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnBaselines(store: LeaseReturnBaselineStore, query: LeaseReturnBaselineQuery): MutableList<LeaseReturnBaseline> {
    return query.apply(store.list())
}
