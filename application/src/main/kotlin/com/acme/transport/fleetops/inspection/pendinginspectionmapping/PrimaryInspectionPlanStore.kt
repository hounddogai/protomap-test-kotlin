package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection plans by code. */
interface PrimaryInspectionPlanStore {
    fun find(code: String): PrimaryInspectionPlan?
    fun save(item: PrimaryInspectionPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionPlanStore(
    var items: MutableMap<String, PrimaryInspectionPlan>,
    var order: MutableList<String>,
) : PrimaryInspectionPlanStore {
    override fun find(code: String): PrimaryInspectionPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionPlan) {
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

    override fun list(): MutableList<PrimaryInspectionPlan> {
        val out = mutableListOf<PrimaryInspectionPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionPlanStore(): PrimaryInspectionPlanStore {
    return MemoryPrimaryInspectionPlanStore(
        mutableMapOf<String, PrimaryInspectionPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionPlanStore(
    var inner: PrimaryInspectionPlanStore,
    var cache: TtlCache<PrimaryInspectionPlan>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionPlanStore {
    override fun find(code: String): PrimaryInspectionPlan? {
        val hit = (cache.get(code) as PrimaryInspectionPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionPlan> = inner.list()
}

fun newCachedPrimaryInspectionPlanStore(inner: PrimaryInspectionPlanStore, ttl: Int, clock: Clock): PrimaryInspectionPlanStore {
    return CachedPrimaryInspectionPlanStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection plans. Empty fields match all. */
data class PrimaryInspectionPlanQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryInspectionPlan>): MutableList<PrimaryInspectionPlan> {
        val matched = filter(items) { candidate: PrimaryInspectionPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionPlans(store: PrimaryInspectionPlanStore, query: PrimaryInspectionPlanQuery): MutableList<PrimaryInspectionPlan> {
    return query.apply(store.list())
}
