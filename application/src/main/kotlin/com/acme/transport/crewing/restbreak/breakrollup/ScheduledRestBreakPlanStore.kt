package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rest break plans by code. */
interface ScheduledRestBreakPlanStore {
    fun find(code: String): ScheduledRestBreakPlan?
    fun save(item: ScheduledRestBreakPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRestBreakPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRestBreakPlanStore(
    var items: MutableMap<String, ScheduledRestBreakPlan>,
    var order: MutableList<String>,
) : ScheduledRestBreakPlanStore {
    override fun find(code: String): ScheduledRestBreakPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRestBreakPlan) {
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

    override fun list(): MutableList<ScheduledRestBreakPlan> {
        val out = mutableListOf<ScheduledRestBreakPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRestBreakPlanStore(): ScheduledRestBreakPlanStore {
    return MemoryScheduledRestBreakPlanStore(
        mutableMapOf<String, ScheduledRestBreakPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRestBreakPlanStore(
    var inner: ScheduledRestBreakPlanStore,
    var cache: TtlCache<ScheduledRestBreakPlan>,
    var hits: Int,
    var misses: Int,
) : ScheduledRestBreakPlanStore {
    override fun find(code: String): ScheduledRestBreakPlan? {
        val hit = (cache.get(code) as ScheduledRestBreakPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRestBreakPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRestBreakPlan> = inner.list()
}

fun newCachedScheduledRestBreakPlanStore(inner: ScheduledRestBreakPlanStore, ttl: Int, clock: Clock): ScheduledRestBreakPlanStore {
    return CachedScheduledRestBreakPlanStore(
        inner = inner,
        cache = newTtlCache<ScheduledRestBreakPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rest break plans. Empty fields match all. */
data class ScheduledRestBreakPlanQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRestBreakPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRestBreakPlan>): MutableList<ScheduledRestBreakPlan> {
        val matched = filter(items) { candidate: ScheduledRestBreakPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRestBreakPlans(store: ScheduledRestBreakPlanStore, query: ScheduledRestBreakPlanQuery): MutableList<ScheduledRestBreakPlan> {
    return query.apply(store.list())
}
