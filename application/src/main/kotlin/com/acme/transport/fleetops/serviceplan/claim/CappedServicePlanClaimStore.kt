package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped service plan claims by code. */
interface CappedServicePlanClaimStore {
    fun find(code: String): CappedServicePlanClaim?
    fun save(item: CappedServicePlanClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServicePlanClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServicePlanClaimStore(
    var items: MutableMap<String, CappedServicePlanClaim>,
    var order: MutableList<String>,
) : CappedServicePlanClaimStore {
    override fun find(code: String): CappedServicePlanClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServicePlanClaim) {
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

    override fun list(): MutableList<CappedServicePlanClaim> {
        val out = mutableListOf<CappedServicePlanClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServicePlanClaimStore(): CappedServicePlanClaimStore {
    return MemoryCappedServicePlanClaimStore(
        mutableMapOf<String, CappedServicePlanClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServicePlanClaimStore(
    var inner: CappedServicePlanClaimStore,
    var cache: TtlCache<CappedServicePlanClaim>,
    var hits: Int,
    var misses: Int,
) : CappedServicePlanClaimStore {
    override fun find(code: String): CappedServicePlanClaim? {
        val hit = (cache.get(code) as CappedServicePlanClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServicePlanClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServicePlanClaim> = inner.list()
}

fun newCachedCappedServicePlanClaimStore(inner: CappedServicePlanClaimStore, ttl: Int, clock: Clock): CappedServicePlanClaimStore {
    return CachedCappedServicePlanClaimStore(
        inner = inner,
        cache = newTtlCache<CappedServicePlanClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service plan claims. Empty fields match all. */
data class CappedServicePlanClaimQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServicePlanClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CappedServicePlanClaim>): MutableList<CappedServicePlanClaim> {
        val matched = filter(items) { candidate: CappedServicePlanClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServicePlanClaims(store: CappedServicePlanClaimStore, query: CappedServicePlanClaimQuery): MutableList<CappedServicePlanClaim> {
    return query.apply(store.list())
}
