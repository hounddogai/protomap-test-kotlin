package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell plans by code. */
interface DwellPlanStore {
    fun find(code: String): DwellPlan?
    fun save(item: DwellPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellPlanStore(
    var items: MutableMap<String, DwellPlan>,
    var order: MutableList<String>,
) : DwellPlanStore {
    override fun find(code: String): DwellPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellPlan) {
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

    override fun list(): MutableList<DwellPlan> {
        val out = mutableListOf<DwellPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellPlanStore(): DwellPlanStore {
    return MemoryDwellPlanStore(mutableMapOf<String, DwellPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellPlanStore(
    var inner: DwellPlanStore,
    var cache: TtlCache<DwellPlan>,
    var hits: Int,
    var misses: Int,
) : DwellPlanStore {
    override fun find(code: String): DwellPlan? {
        val hit = (cache.get(code) as DwellPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellPlan> = inner.list()
}

fun newCachedDwellPlanStore(inner: DwellPlanStore, ttl: Int, clock: Clock): DwellPlanStore {
    return CachedDwellPlanStore(
        inner = inner,
        cache = newTtlCache<DwellPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell plans. Empty fields match all. */
data class DwellPlanQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellPlan>): MutableList<DwellPlan> {
        val matched = filter(items) { candidate: DwellPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellPlans(store: DwellPlanStore, query: DwellPlanQuery): MutableList<DwellPlan> {
    return query.apply(store.list())
}
