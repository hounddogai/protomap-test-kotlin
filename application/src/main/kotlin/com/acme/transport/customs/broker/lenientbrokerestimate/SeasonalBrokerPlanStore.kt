package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal broker plans by code. */
interface SeasonalBrokerPlanStore {
    fun find(code: String): SeasonalBrokerPlan?
    fun save(item: SeasonalBrokerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalBrokerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalBrokerPlanStore(
    var items: MutableMap<String, SeasonalBrokerPlan>,
    var order: MutableList<String>,
) : SeasonalBrokerPlanStore {
    override fun find(code: String): SeasonalBrokerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalBrokerPlan) {
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

    override fun list(): MutableList<SeasonalBrokerPlan> {
        val out = mutableListOf<SeasonalBrokerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalBrokerPlanStore(): SeasonalBrokerPlanStore {
    return MemorySeasonalBrokerPlanStore(mutableMapOf<String, SeasonalBrokerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalBrokerPlanStore(
    var inner: SeasonalBrokerPlanStore,
    var cache: TtlCache<SeasonalBrokerPlan>,
    var hits: Int,
    var misses: Int,
) : SeasonalBrokerPlanStore {
    override fun find(code: String): SeasonalBrokerPlan? {
        val hit = (cache.get(code) as SeasonalBrokerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalBrokerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalBrokerPlan> = inner.list()
}

fun newCachedSeasonalBrokerPlanStore(inner: SeasonalBrokerPlanStore, ttl: Int, clock: Clock): SeasonalBrokerPlanStore {
    return CachedSeasonalBrokerPlanStore(
        inner = inner,
        cache = newTtlCache<SeasonalBrokerPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal broker plans. Empty fields match all. */
data class SeasonalBrokerPlanQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalBrokerPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalBrokerPlan>): MutableList<SeasonalBrokerPlan> {
        val matched = filter(items) { candidate: SeasonalBrokerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalBrokerPlans(store: SeasonalBrokerPlanStore, query: SeasonalBrokerPlanQuery): MutableList<SeasonalBrokerPlan> {
    return query.apply(store.list())
}
