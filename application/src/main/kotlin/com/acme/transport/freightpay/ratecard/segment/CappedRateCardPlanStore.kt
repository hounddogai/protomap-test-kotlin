package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rate card plans by code. */
interface CappedRateCardPlanStore {
    fun find(code: String): CappedRateCardPlan?
    fun save(item: CappedRateCardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRateCardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRateCardPlanStore(
    var items: MutableMap<String, CappedRateCardPlan>,
    var order: MutableList<String>,
) : CappedRateCardPlanStore {
    override fun find(code: String): CappedRateCardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRateCardPlan) {
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

    override fun list(): MutableList<CappedRateCardPlan> {
        val out = mutableListOf<CappedRateCardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRateCardPlanStore(): CappedRateCardPlanStore {
    return MemoryCappedRateCardPlanStore(mutableMapOf<String, CappedRateCardPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRateCardPlanStore(
    var inner: CappedRateCardPlanStore,
    var cache: TtlCache<CappedRateCardPlan>,
    var hits: Int,
    var misses: Int,
) : CappedRateCardPlanStore {
    override fun find(code: String): CappedRateCardPlan? {
        val hit = (cache.get(code) as CappedRateCardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRateCardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRateCardPlan> = inner.list()
}

fun newCachedCappedRateCardPlanStore(inner: CappedRateCardPlanStore, ttl: Int, clock: Clock): CappedRateCardPlanStore {
    return CachedCappedRateCardPlanStore(
        inner = inner,
        cache = newTtlCache<CappedRateCardPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rate card plans. Empty fields match all. */
data class CappedRateCardPlanQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRateCardPlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CappedRateCardPlan>): MutableList<CappedRateCardPlan> {
        val matched = filter(items) { candidate: CappedRateCardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRateCardPlans(store: CappedRateCardPlanStore, query: CappedRateCardPlanQuery): MutableList<CappedRateCardPlan> {
    return query.apply(store.list())
}
