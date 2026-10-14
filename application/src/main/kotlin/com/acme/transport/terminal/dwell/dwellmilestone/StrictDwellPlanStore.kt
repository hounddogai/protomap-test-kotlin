package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dwell plans by code. */
interface StrictDwellPlanStore {
    fun find(code: String): StrictDwellPlan?
    fun save(item: StrictDwellPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDwellPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDwellPlanStore(
    var items: MutableMap<String, StrictDwellPlan>,
    var order: MutableList<String>,
) : StrictDwellPlanStore {
    override fun find(code: String): StrictDwellPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDwellPlan) {
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

    override fun list(): MutableList<StrictDwellPlan> {
        val out = mutableListOf<StrictDwellPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDwellPlanStore(): StrictDwellPlanStore {
    return MemoryStrictDwellPlanStore(mutableMapOf<String, StrictDwellPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDwellPlanStore(
    var inner: StrictDwellPlanStore,
    var cache: TtlCache<StrictDwellPlan>,
    var hits: Int,
    var misses: Int,
) : StrictDwellPlanStore {
    override fun find(code: String): StrictDwellPlan? {
        val hit = (cache.get(code) as StrictDwellPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDwellPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDwellPlan> = inner.list()
}

fun newCachedStrictDwellPlanStore(inner: StrictDwellPlanStore, ttl: Int, clock: Clock): StrictDwellPlanStore {
    return CachedStrictDwellPlanStore(
        inner = inner,
        cache = newTtlCache<StrictDwellPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dwell plans. Empty fields match all. */
data class StrictDwellPlanQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDwellPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDwellPlan>): MutableList<StrictDwellPlan> {
        val matched = filter(items) { candidate: StrictDwellPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDwellPlans(store: StrictDwellPlanStore, query: StrictDwellPlanQuery): MutableList<StrictDwellPlan> {
    return query.apply(store.list())
}
