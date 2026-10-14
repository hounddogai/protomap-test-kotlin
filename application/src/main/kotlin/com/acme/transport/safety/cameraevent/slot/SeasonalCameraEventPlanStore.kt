package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal camera event plans by code. */
interface SeasonalCameraEventPlanStore {
    fun find(code: String): SeasonalCameraEventPlan?
    fun save(item: SeasonalCameraEventPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCameraEventPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCameraEventPlanStore(
    var items: MutableMap<String, SeasonalCameraEventPlan>,
    var order: MutableList<String>,
) : SeasonalCameraEventPlanStore {
    override fun find(code: String): SeasonalCameraEventPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCameraEventPlan) {
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

    override fun list(): MutableList<SeasonalCameraEventPlan> {
        val out = mutableListOf<SeasonalCameraEventPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCameraEventPlanStore(): SeasonalCameraEventPlanStore {
    return MemorySeasonalCameraEventPlanStore(
        mutableMapOf<String, SeasonalCameraEventPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCameraEventPlanStore(
    var inner: SeasonalCameraEventPlanStore,
    var cache: TtlCache<SeasonalCameraEventPlan>,
    var hits: Int,
    var misses: Int,
) : SeasonalCameraEventPlanStore {
    override fun find(code: String): SeasonalCameraEventPlan? {
        val hit = (cache.get(code) as SeasonalCameraEventPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCameraEventPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCameraEventPlan> = inner.list()
}

fun newCachedSeasonalCameraEventPlanStore(inner: SeasonalCameraEventPlanStore, ttl: Int, clock: Clock): SeasonalCameraEventPlanStore {
    return CachedSeasonalCameraEventPlanStore(
        inner = inner,
        cache = newTtlCache<SeasonalCameraEventPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal camera event plans. Empty fields match all. */
data class SeasonalCameraEventPlanQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCameraEventPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<SeasonalCameraEventPlan>): MutableList<SeasonalCameraEventPlan> {
        val matched = filter(items) { candidate: SeasonalCameraEventPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCameraEventPlans(store: SeasonalCameraEventPlanStore, query: SeasonalCameraEventPlanQuery): MutableList<SeasonalCameraEventPlan> {
    return query.apply(store.list())
}
