package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card plans by code. */
interface StrictRateCardPlanStore {
    fun find(code: String): StrictRateCardPlan?
    fun save(item: StrictRateCardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardPlanStore(
    var items: MutableMap<String, StrictRateCardPlan>,
    var order: MutableList<String>,
) : StrictRateCardPlanStore {
    override fun find(code: String): StrictRateCardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardPlan) {
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

    override fun list(): MutableList<StrictRateCardPlan> {
        val out = mutableListOf<StrictRateCardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardPlanStore(): StrictRateCardPlanStore {
    return MemoryStrictRateCardPlanStore(mutableMapOf<String, StrictRateCardPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardPlanStore(
    var inner: StrictRateCardPlanStore,
    var cache: TtlCache<StrictRateCardPlan>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardPlanStore {
    override fun find(code: String): StrictRateCardPlan? {
        val hit = (cache.get(code) as StrictRateCardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardPlan> = inner.list()
}

fun newCachedStrictRateCardPlanStore(inner: StrictRateCardPlanStore, ttl: Int, clock: Clock): StrictRateCardPlanStore {
    return CachedStrictRateCardPlanStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card plans. Empty fields match all. */
data class StrictRateCardPlanQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardPlan>): MutableList<StrictRateCardPlan> {
        val matched = filter(items) { candidate: StrictRateCardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardPlans(store: StrictRateCardPlanStore, query: StrictRateCardPlanQuery): MutableList<StrictRateCardPlan> {
    return query.apply(store.list())
}
