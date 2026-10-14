package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive incident plans by code. */
interface AdaptiveIncidentPlanStore {
    fun find(code: String): AdaptiveIncidentPlan?
    fun save(item: AdaptiveIncidentPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveIncidentPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveIncidentPlanStore(
    var items: MutableMap<String, AdaptiveIncidentPlan>,
    var order: MutableList<String>,
) : AdaptiveIncidentPlanStore {
    override fun find(code: String): AdaptiveIncidentPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveIncidentPlan) {
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

    override fun list(): MutableList<AdaptiveIncidentPlan> {
        val out = mutableListOf<AdaptiveIncidentPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveIncidentPlanStore(): AdaptiveIncidentPlanStore {
    return MemoryAdaptiveIncidentPlanStore(
        mutableMapOf<String, AdaptiveIncidentPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveIncidentPlanStore(
    var inner: AdaptiveIncidentPlanStore,
    var cache: TtlCache<AdaptiveIncidentPlan>,
    var hits: Int,
    var misses: Int,
) : AdaptiveIncidentPlanStore {
    override fun find(code: String): AdaptiveIncidentPlan? {
        val hit = (cache.get(code) as AdaptiveIncidentPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveIncidentPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveIncidentPlan> = inner.list()
}

fun newCachedAdaptiveIncidentPlanStore(inner: AdaptiveIncidentPlanStore, ttl: Int, clock: Clock): AdaptiveIncidentPlanStore {
    return CachedAdaptiveIncidentPlanStore(
        inner = inner,
        cache = newTtlCache<AdaptiveIncidentPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive incident plans. Empty fields match all. */
data class AdaptiveIncidentPlanQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveIncidentPlan): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveIncidentPlan>): MutableList<AdaptiveIncidentPlan> {
        val matched = filter(items) { candidate: AdaptiveIncidentPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveIncidentPlans(store: AdaptiveIncidentPlanStore, query: AdaptiveIncidentPlanQuery): MutableList<AdaptiveIncidentPlan> {
    return query.apply(store.list())
}
