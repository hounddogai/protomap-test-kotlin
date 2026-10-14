package com.acme.transport.freightpay.audit.claim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted audit claims by code. */
interface WeightedAuditClaimStore {
    fun find(code: String): WeightedAuditClaim?
    fun save(item: WeightedAuditClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAuditClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAuditClaimStore(
    var items: MutableMap<String, WeightedAuditClaim>,
    var order: MutableList<String>,
) : WeightedAuditClaimStore {
    override fun find(code: String): WeightedAuditClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAuditClaim) {
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

    override fun list(): MutableList<WeightedAuditClaim> {
        val out = mutableListOf<WeightedAuditClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAuditClaimStore(): WeightedAuditClaimStore {
    return MemoryWeightedAuditClaimStore(mutableMapOf<String, WeightedAuditClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAuditClaimStore(
    var inner: WeightedAuditClaimStore,
    var cache: TtlCache<WeightedAuditClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedAuditClaimStore {
    override fun find(code: String): WeightedAuditClaim? {
        val hit = (cache.get(code) as WeightedAuditClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAuditClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAuditClaim> = inner.list()
}

fun newCachedWeightedAuditClaimStore(inner: WeightedAuditClaimStore, ttl: Int, clock: Clock): WeightedAuditClaimStore {
    return CachedWeightedAuditClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedAuditClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted audit claims. Empty fields match all. */
data class WeightedAuditClaimQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAuditClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<WeightedAuditClaim>): MutableList<WeightedAuditClaim> {
        val matched = filter(items) { candidate: WeightedAuditClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAuditClaims(store: WeightedAuditClaimStore, query: WeightedAuditClaimQuery): MutableList<WeightedAuditClaim> {
    return query.apply(store.list())
}
