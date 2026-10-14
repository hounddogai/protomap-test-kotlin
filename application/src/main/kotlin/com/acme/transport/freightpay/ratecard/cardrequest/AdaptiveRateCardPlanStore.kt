package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive rate card plans by code. */
interface AdaptiveRateCardPlanStore {
    fun find(code: String): AdaptiveRateCardPlan?
    fun save(item: AdaptiveRateCardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRateCardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRateCardPlanStore(
    var items: MutableMap<String, AdaptiveRateCardPlan>,
    var order: MutableList<String>,
) : AdaptiveRateCardPlanStore {
    override fun find(code: String): AdaptiveRateCardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRateCardPlan) {
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

    override fun list(): MutableList<AdaptiveRateCardPlan> {
        val out = mutableListOf<AdaptiveRateCardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRateCardPlanStore(): AdaptiveRateCardPlanStore {
    return MemoryAdaptiveRateCardPlanStore(
        mutableMapOf<String, AdaptiveRateCardPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRateCardPlanStore(
    var inner: AdaptiveRateCardPlanStore,
    var cache: TtlCache<AdaptiveRateCardPlan>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRateCardPlanStore {
    override fun find(code: String): AdaptiveRateCardPlan? {
        val hit = (cache.get(code) as AdaptiveRateCardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRateCardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRateCardPlan> = inner.list()
}

fun newCachedAdaptiveRateCardPlanStore(inner: AdaptiveRateCardPlanStore, ttl: Int, clock: Clock): AdaptiveRateCardPlanStore {
    return CachedAdaptiveRateCardPlanStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRateCardPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive rate card plans. Empty fields match all. */
data class AdaptiveRateCardPlanQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRateCardPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRateCardPlan>): MutableList<AdaptiveRateCardPlan> {
        val matched = filter(items) { candidate: AdaptiveRateCardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRateCardPlans(store: AdaptiveRateCardPlanStore, query: AdaptiveRateCardPlanQuery): MutableList<AdaptiveRateCardPlan> {
    return query.apply(store.list())
}
