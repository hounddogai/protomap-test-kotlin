package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dwell plans by code. */
interface RollingDwellPlanStore {
    fun find(code: String): RollingDwellPlan?
    fun save(item: RollingDwellPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDwellPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDwellPlanStore(
    var items: MutableMap<String, RollingDwellPlan>,
    var order: MutableList<String>,
) : RollingDwellPlanStore {
    override fun find(code: String): RollingDwellPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDwellPlan) {
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

    override fun list(): MutableList<RollingDwellPlan> {
        val out = mutableListOf<RollingDwellPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDwellPlanStore(): RollingDwellPlanStore {
    return MemoryRollingDwellPlanStore(mutableMapOf<String, RollingDwellPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDwellPlanStore(
    var inner: RollingDwellPlanStore,
    var cache: TtlCache<RollingDwellPlan>,
    var hits: Int,
    var misses: Int,
) : RollingDwellPlanStore {
    override fun find(code: String): RollingDwellPlan? {
        val hit = (cache.get(code) as RollingDwellPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDwellPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDwellPlan> = inner.list()
}

fun newCachedRollingDwellPlanStore(inner: RollingDwellPlanStore, ttl: Int, clock: Clock): RollingDwellPlanStore {
    return CachedRollingDwellPlanStore(
        inner = inner,
        cache = newTtlCache<RollingDwellPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dwell plans. Empty fields match all. */
data class RollingDwellPlanQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDwellPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDwellPlan>): MutableList<RollingDwellPlan> {
        val matched = filter(items) { candidate: RollingDwellPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDwellPlans(store: RollingDwellPlanStore, query: RollingDwellPlanQuery): MutableList<RollingDwellPlan> {
    return query.apply(store.list())
}
