package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict service plan claims by code. */
interface StrictServicePlanClaimStore {
    fun find(code: String): StrictServicePlanClaim?
    fun save(item: StrictServicePlanClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictServicePlanClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictServicePlanClaimStore(
    var items: MutableMap<String, StrictServicePlanClaim>,
    var order: MutableList<String>,
) : StrictServicePlanClaimStore {
    override fun find(code: String): StrictServicePlanClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictServicePlanClaim) {
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

    override fun list(): MutableList<StrictServicePlanClaim> {
        val out = mutableListOf<StrictServicePlanClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictServicePlanClaimStore(): StrictServicePlanClaimStore {
    return MemoryStrictServicePlanClaimStore(
        mutableMapOf<String, StrictServicePlanClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictServicePlanClaimStore(
    var inner: StrictServicePlanClaimStore,
    var cache: TtlCache<StrictServicePlanClaim>,
    var hits: Int,
    var misses: Int,
) : StrictServicePlanClaimStore {
    override fun find(code: String): StrictServicePlanClaim? {
        val hit = (cache.get(code) as StrictServicePlanClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictServicePlanClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictServicePlanClaim> = inner.list()
}

fun newCachedStrictServicePlanClaimStore(inner: StrictServicePlanClaimStore, ttl: Int, clock: Clock): StrictServicePlanClaimStore {
    return CachedStrictServicePlanClaimStore(
        inner = inner,
        cache = newTtlCache<StrictServicePlanClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict service plan claims. Empty fields match all. */
data class StrictServicePlanClaimQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictServicePlanClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictServicePlanClaim>): MutableList<StrictServicePlanClaim> {
        val matched = filter(items) { candidate: StrictServicePlanClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictServicePlanClaims(store: StrictServicePlanClaimStore, query: StrictServicePlanClaimQuery): MutableList<StrictServicePlanClaim> {
    return query.apply(store.list())
}
