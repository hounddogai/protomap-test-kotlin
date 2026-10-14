package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling roster plans by code. */
interface RollingRosterPlanStore {
    fun find(code: String): RollingRosterPlan?
    fun save(item: RollingRosterPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRosterPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRosterPlanStore(
    var items: MutableMap<String, RollingRosterPlan>,
    var order: MutableList<String>,
) : RollingRosterPlanStore {
    override fun find(code: String): RollingRosterPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRosterPlan) {
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

    override fun list(): MutableList<RollingRosterPlan> {
        val out = mutableListOf<RollingRosterPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRosterPlanStore(): RollingRosterPlanStore {
    return MemoryRollingRosterPlanStore(mutableMapOf<String, RollingRosterPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRosterPlanStore(
    var inner: RollingRosterPlanStore,
    var cache: TtlCache<RollingRosterPlan>,
    var hits: Int,
    var misses: Int,
) : RollingRosterPlanStore {
    override fun find(code: String): RollingRosterPlan? {
        val hit = (cache.get(code) as RollingRosterPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRosterPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRosterPlan> = inner.list()
}

fun newCachedRollingRosterPlanStore(inner: RollingRosterPlanStore, ttl: Int, clock: Clock): RollingRosterPlanStore {
    return CachedRollingRosterPlanStore(
        inner = inner,
        cache = newTtlCache<RollingRosterPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling roster plans. Empty fields match all. */
data class RollingRosterPlanQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRosterPlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RollingRosterPlan>): MutableList<RollingRosterPlan> {
        val matched = filter(items) { candidate: RollingRosterPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRosterPlans(store: RollingRosterPlanStore, query: RollingRosterPlanQuery): MutableList<RollingRosterPlan> {
    return query.apply(store.list())
}
