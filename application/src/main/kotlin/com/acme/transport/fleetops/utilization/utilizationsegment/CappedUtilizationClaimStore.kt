package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped utilization claims by code. */
interface CappedUtilizationClaimStore {
    fun find(code: String): CappedUtilizationClaim?
    fun save(item: CappedUtilizationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedUtilizationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedUtilizationClaimStore(
    var items: MutableMap<String, CappedUtilizationClaim>,
    var order: MutableList<String>,
) : CappedUtilizationClaimStore {
    override fun find(code: String): CappedUtilizationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedUtilizationClaim) {
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

    override fun list(): MutableList<CappedUtilizationClaim> {
        val out = mutableListOf<CappedUtilizationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedUtilizationClaimStore(): CappedUtilizationClaimStore {
    return MemoryCappedUtilizationClaimStore(
        mutableMapOf<String, CappedUtilizationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedUtilizationClaimStore(
    var inner: CappedUtilizationClaimStore,
    var cache: TtlCache<CappedUtilizationClaim>,
    var hits: Int,
    var misses: Int,
) : CappedUtilizationClaimStore {
    override fun find(code: String): CappedUtilizationClaim? {
        val hit = (cache.get(code) as CappedUtilizationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedUtilizationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedUtilizationClaim> = inner.list()
}

fun newCachedCappedUtilizationClaimStore(inner: CappedUtilizationClaimStore, ttl: Int, clock: Clock): CappedUtilizationClaimStore {
    return CachedCappedUtilizationClaimStore(
        inner = inner,
        cache = newTtlCache<CappedUtilizationClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped utilization claims. Empty fields match all. */
data class CappedUtilizationClaimQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedUtilizationClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CappedUtilizationClaim>): MutableList<CappedUtilizationClaim> {
        val matched = filter(items) { candidate: CappedUtilizationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedUtilizationClaims(store: CappedUtilizationClaimStore, query: CappedUtilizationClaimQuery): MutableList<CappedUtilizationClaim> {
    return query.apply(store.list())
}
