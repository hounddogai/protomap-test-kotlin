package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card plans by code. */
interface RateCardPlanStore {
    fun find(code: String): RateCardPlan?
    fun save(item: RateCardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardPlanStore(
    var items: MutableMap<String, RateCardPlan>,
    var order: MutableList<String>,
) : RateCardPlanStore {
    override fun find(code: String): RateCardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardPlan) {
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

    override fun list(): MutableList<RateCardPlan> {
        val out = mutableListOf<RateCardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardPlanStore(): RateCardPlanStore {
    return MemoryRateCardPlanStore(mutableMapOf<String, RateCardPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardPlanStore(
    var inner: RateCardPlanStore,
    var cache: TtlCache<RateCardPlan>,
    var hits: Int,
    var misses: Int,
) : RateCardPlanStore {
    override fun find(code: String): RateCardPlan? {
        val hit = (cache.get(code) as RateCardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardPlan> = inner.list()
}

fun newCachedRateCardPlanStore(inner: RateCardPlanStore, ttl: Int, clock: Clock): RateCardPlanStore {
    return CachedRateCardPlanStore(
        inner = inner,
        cache = newTtlCache<RateCardPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card plans. Empty fields match all. */
data class RateCardPlanQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardPlan): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardPlan>): MutableList<RateCardPlan> {
        val matched = filter(items) { candidate: RateCardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardPlans(store: RateCardPlanStore, query: RateCardPlanQuery): MutableList<RateCardPlan> {
    return query.apply(store.list())
}
