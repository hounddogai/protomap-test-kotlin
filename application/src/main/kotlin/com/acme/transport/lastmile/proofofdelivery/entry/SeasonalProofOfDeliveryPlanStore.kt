package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal proof of delivery plans by code. */
interface SeasonalProofOfDeliveryPlanStore {
    fun find(code: String): SeasonalProofOfDeliveryPlan?
    fun save(item: SeasonalProofOfDeliveryPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalProofOfDeliveryPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalProofOfDeliveryPlanStore(
    var items: MutableMap<String, SeasonalProofOfDeliveryPlan>,
    var order: MutableList<String>,
) : SeasonalProofOfDeliveryPlanStore {
    override fun find(code: String): SeasonalProofOfDeliveryPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalProofOfDeliveryPlan) {
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

    override fun list(): MutableList<SeasonalProofOfDeliveryPlan> {
        val out = mutableListOf<SeasonalProofOfDeliveryPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalProofOfDeliveryPlanStore(): SeasonalProofOfDeliveryPlanStore {
    return MemorySeasonalProofOfDeliveryPlanStore(
        mutableMapOf<String, SeasonalProofOfDeliveryPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalProofOfDeliveryPlanStore(
    var inner: SeasonalProofOfDeliveryPlanStore,
    var cache: TtlCache<SeasonalProofOfDeliveryPlan>,
    var hits: Int,
    var misses: Int,
) : SeasonalProofOfDeliveryPlanStore {
    override fun find(code: String): SeasonalProofOfDeliveryPlan? {
        val hit = (cache.get(code) as SeasonalProofOfDeliveryPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalProofOfDeliveryPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalProofOfDeliveryPlan> = inner.list()
}

fun newCachedSeasonalProofOfDeliveryPlanStore(inner: SeasonalProofOfDeliveryPlanStore, ttl: Int, clock: Clock): SeasonalProofOfDeliveryPlanStore {
    return CachedSeasonalProofOfDeliveryPlanStore(
        inner = inner,
        cache = newTtlCache<SeasonalProofOfDeliveryPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal proof of delivery plans. Empty fields match all. */
data class SeasonalProofOfDeliveryPlanQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalProofOfDeliveryPlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalProofOfDeliveryPlan>): MutableList<SeasonalProofOfDeliveryPlan> {
        val matched = filter(items) { candidate: SeasonalProofOfDeliveryPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalProofOfDeliveryPlans(store: SeasonalProofOfDeliveryPlanStore, query: SeasonalProofOfDeliveryPlanQuery): MutableList<SeasonalProofOfDeliveryPlan> {
    return query.apply(store.list())
}
