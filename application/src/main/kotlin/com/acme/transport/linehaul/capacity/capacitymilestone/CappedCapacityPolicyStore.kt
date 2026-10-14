package com.acme.transport.linehaul.capacity.capacitymilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped capacity policies by code. */
interface CappedCapacityPolicyStore {
    fun find(code: String): CappedCapacityPolicy?
    fun save(item: CappedCapacityPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCapacityPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCapacityPolicyStore(
    var items: MutableMap<String, CappedCapacityPolicy>,
    var order: MutableList<String>,
) : CappedCapacityPolicyStore {
    override fun find(code: String): CappedCapacityPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCapacityPolicy) {
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

    override fun list(): MutableList<CappedCapacityPolicy> {
        val out = mutableListOf<CappedCapacityPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCapacityPolicyStore(): CappedCapacityPolicyStore {
    return MemoryCappedCapacityPolicyStore(
        mutableMapOf<String, CappedCapacityPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCapacityPolicyStore(
    var inner: CappedCapacityPolicyStore,
    var cache: TtlCache<CappedCapacityPolicy>,
    var hits: Int,
    var misses: Int,
) : CappedCapacityPolicyStore {
    override fun find(code: String): CappedCapacityPolicy? {
        val hit = (cache.get(code) as CappedCapacityPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCapacityPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCapacityPolicy> = inner.list()
}

fun newCachedCappedCapacityPolicyStore(inner: CappedCapacityPolicyStore, ttl: Int, clock: Clock): CappedCapacityPolicyStore {
    return CachedCappedCapacityPolicyStore(
        inner = inner,
        cache = newTtlCache<CappedCapacityPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped capacity policies. Empty fields match all. */
data class CappedCapacityPolicyQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCapacityPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCapacityPolicy>): MutableList<CappedCapacityPolicy> {
        val matched = filter(items) { candidate: CappedCapacityPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCapacityPolicies(store: CappedCapacityPolicyStore, query: CappedCapacityPolicyQuery): MutableList<CappedCapacityPolicy> {
    return query.apply(store.list())
}
