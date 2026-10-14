package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual roster plans by code. */
interface ManualRosterPlanStore {
    fun find(code: String): ManualRosterPlan?
    fun save(item: ManualRosterPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRosterPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRosterPlanStore(
    var items: MutableMap<String, ManualRosterPlan>,
    var order: MutableList<String>,
) : ManualRosterPlanStore {
    override fun find(code: String): ManualRosterPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRosterPlan) {
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

    override fun list(): MutableList<ManualRosterPlan> {
        val out = mutableListOf<ManualRosterPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRosterPlanStore(): ManualRosterPlanStore {
    return MemoryManualRosterPlanStore(mutableMapOf<String, ManualRosterPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRosterPlanStore(
    var inner: ManualRosterPlanStore,
    var cache: TtlCache<ManualRosterPlan>,
    var hits: Int,
    var misses: Int,
) : ManualRosterPlanStore {
    override fun find(code: String): ManualRosterPlan? {
        val hit = (cache.get(code) as ManualRosterPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRosterPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRosterPlan> = inner.list()
}

fun newCachedManualRosterPlanStore(inner: ManualRosterPlanStore, ttl: Int, clock: Clock): ManualRosterPlanStore {
    return CachedManualRosterPlanStore(
        inner = inner,
        cache = newTtlCache<ManualRosterPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual roster plans. Empty fields match all. */
data class ManualRosterPlanQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRosterPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRosterPlan>): MutableList<ManualRosterPlan> {
        val matched = filter(items) { candidate: ManualRosterPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRosterPlans(store: ManualRosterPlanStore, query: ManualRosterPlanQuery): MutableList<ManualRosterPlan> {
    return query.apply(store.list())
}
