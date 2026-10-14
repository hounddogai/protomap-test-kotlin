package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional trailer plans by code. */
interface RegionalTrailerPlanStore {
    fun find(code: String): RegionalTrailerPlan?
    fun save(item: RegionalTrailerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerPlanStore(
    var items: MutableMap<String, RegionalTrailerPlan>,
    var order: MutableList<String>,
) : RegionalTrailerPlanStore {
    override fun find(code: String): RegionalTrailerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerPlan) {
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

    override fun list(): MutableList<RegionalTrailerPlan> {
        val out = mutableListOf<RegionalTrailerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerPlanStore(): RegionalTrailerPlanStore {
    return MemoryRegionalTrailerPlanStore(mutableMapOf<String, RegionalTrailerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerPlanStore(
    var inner: RegionalTrailerPlanStore,
    var cache: TtlCache<RegionalTrailerPlan>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerPlanStore {
    override fun find(code: String): RegionalTrailerPlan? {
        val hit = (cache.get(code) as RegionalTrailerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerPlan> = inner.list()
}

fun newCachedRegionalTrailerPlanStore(inner: RegionalTrailerPlanStore, ttl: Int, clock: Clock): RegionalTrailerPlanStore {
    return CachedRegionalTrailerPlanStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer plans. Empty fields match all. */
data class RegionalTrailerPlanQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerPlan>): MutableList<RegionalTrailerPlan> {
        val matched = filter(items) { candidate: RegionalTrailerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerPlans(store: RegionalTrailerPlanStore, query: RegionalTrailerPlanQuery): MutableList<RegionalTrailerPlan> {
    return query.apply(store.list())
}
