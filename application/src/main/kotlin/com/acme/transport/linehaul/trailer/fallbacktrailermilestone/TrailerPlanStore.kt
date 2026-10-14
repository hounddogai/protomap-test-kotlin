package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer plans by code. */
interface TrailerPlanStore {
    fun find(code: String): TrailerPlan?
    fun save(item: TrailerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerPlanStore(
    var items: MutableMap<String, TrailerPlan>,
    var order: MutableList<String>,
) : TrailerPlanStore {
    override fun find(code: String): TrailerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerPlan) {
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

    override fun list(): MutableList<TrailerPlan> {
        val out = mutableListOf<TrailerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerPlanStore(): TrailerPlanStore {
    return MemoryTrailerPlanStore(mutableMapOf<String, TrailerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerPlanStore(
    var inner: TrailerPlanStore,
    var cache: TtlCache<TrailerPlan>,
    var hits: Int,
    var misses: Int,
) : TrailerPlanStore {
    override fun find(code: String): TrailerPlan? {
        val hit = (cache.get(code) as TrailerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerPlan> = inner.list()
}

fun newCachedTrailerPlanStore(inner: TrailerPlanStore, ttl: Int, clock: Clock): TrailerPlanStore {
    return CachedTrailerPlanStore(
        inner = inner,
        cache = newTtlCache<TrailerPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer plans. Empty fields match all. */
data class TrailerPlanQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerPlan): Boolean {
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

    fun apply(items: MutableList<TrailerPlan>): MutableList<TrailerPlan> {
        val matched = filter(items) { candidate: TrailerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerPlans(store: TrailerPlanStore, query: TrailerPlanQuery): MutableList<TrailerPlan> {
    return query.apply(store.list())
}
