package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted duty period claims by code. */
interface WeightedDutyPeriodClaimStore {
    fun find(code: String): WeightedDutyPeriodClaim?
    fun save(item: WeightedDutyPeriodClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDutyPeriodClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDutyPeriodClaimStore(
    var items: MutableMap<String, WeightedDutyPeriodClaim>,
    var order: MutableList<String>,
) : WeightedDutyPeriodClaimStore {
    override fun find(code: String): WeightedDutyPeriodClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDutyPeriodClaim) {
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

    override fun list(): MutableList<WeightedDutyPeriodClaim> {
        val out = mutableListOf<WeightedDutyPeriodClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDutyPeriodClaimStore(): WeightedDutyPeriodClaimStore {
    return MemoryWeightedDutyPeriodClaimStore(
        mutableMapOf<String, WeightedDutyPeriodClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDutyPeriodClaimStore(
    var inner: WeightedDutyPeriodClaimStore,
    var cache: TtlCache<WeightedDutyPeriodClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedDutyPeriodClaimStore {
    override fun find(code: String): WeightedDutyPeriodClaim? {
        val hit = (cache.get(code) as WeightedDutyPeriodClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDutyPeriodClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDutyPeriodClaim> = inner.list()
}

fun newCachedWeightedDutyPeriodClaimStore(inner: WeightedDutyPeriodClaimStore, ttl: Int, clock: Clock): WeightedDutyPeriodClaimStore {
    return CachedWeightedDutyPeriodClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedDutyPeriodClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted duty period claims. Empty fields match all. */
data class WeightedDutyPeriodClaimQuery(
    var region: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDutyPeriodClaim): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDutyPeriodClaim>): MutableList<WeightedDutyPeriodClaim> {
        val matched = filter(items) { candidate: WeightedDutyPeriodClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDutyPeriodClaims(store: WeightedDutyPeriodClaimStore, query: WeightedDutyPeriodClaimQuery): MutableList<WeightedDutyPeriodClaim> {
    return query.apply(store.list())
}
