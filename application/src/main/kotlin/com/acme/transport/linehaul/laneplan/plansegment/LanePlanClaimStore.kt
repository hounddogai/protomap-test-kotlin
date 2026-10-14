package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan claims by code. */
interface LanePlanClaimStore {
    fun find(code: String): LanePlanClaim?
    fun save(item: LanePlanClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanClaimStore(
    var items: MutableMap<String, LanePlanClaim>,
    var order: MutableList<String>,
) : LanePlanClaimStore {
    override fun find(code: String): LanePlanClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanClaim) {
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

    override fun list(): MutableList<LanePlanClaim> {
        val out = mutableListOf<LanePlanClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanClaimStore(): LanePlanClaimStore {
    return MemoryLanePlanClaimStore(mutableMapOf<String, LanePlanClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanClaimStore(
    var inner: LanePlanClaimStore,
    var cache: TtlCache<LanePlanClaim>,
    var hits: Int,
    var misses: Int,
) : LanePlanClaimStore {
    override fun find(code: String): LanePlanClaim? {
        val hit = (cache.get(code) as LanePlanClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanClaim> = inner.list()
}

fun newCachedLanePlanClaimStore(inner: LanePlanClaimStore, ttl: Int, clock: Clock): LanePlanClaimStore {
    return CachedLanePlanClaimStore(
        inner = inner,
        cache = newTtlCache<LanePlanClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan claims. Empty fields match all. */
data class LanePlanClaimQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LanePlanClaim>): MutableList<LanePlanClaim> {
        val matched = filter(items) { candidate: LanePlanClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanClaims(store: LanePlanClaimStore, query: LanePlanClaimQuery): MutableList<LanePlanClaim> {
    return query.apply(store.list())
}
