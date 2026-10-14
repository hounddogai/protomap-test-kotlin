package com.acme.transport.crewing.availability.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability plans by code. */
interface AvailabilityPlanStore {
    fun find(code: String): AvailabilityPlan?
    fun save(item: AvailabilityPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityPlanStore(
    var items: MutableMap<String, AvailabilityPlan>,
    var order: MutableList<String>,
) : AvailabilityPlanStore {
    override fun find(code: String): AvailabilityPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityPlan) {
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

    override fun list(): MutableList<AvailabilityPlan> {
        val out = mutableListOf<AvailabilityPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityPlanStore(): AvailabilityPlanStore {
    return MemoryAvailabilityPlanStore(mutableMapOf<String, AvailabilityPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityPlanStore(
    var inner: AvailabilityPlanStore,
    var cache: TtlCache<AvailabilityPlan>,
    var hits: Int,
    var misses: Int,
) : AvailabilityPlanStore {
    override fun find(code: String): AvailabilityPlan? {
        val hit = (cache.get(code) as AvailabilityPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityPlan> = inner.list()
}

fun newCachedAvailabilityPlanStore(inner: AvailabilityPlanStore, ttl: Int, clock: Clock): AvailabilityPlanStore {
    return CachedAvailabilityPlanStore(
        inner = inner,
        cache = newTtlCache<AvailabilityPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability plans. Empty fields match all. */
data class AvailabilityPlanQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AvailabilityPlan>): MutableList<AvailabilityPlan> {
        val matched = filter(items) { candidate: AvailabilityPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityPlans(store: AvailabilityPlanStore, query: AvailabilityPlanQuery): MutableList<AvailabilityPlan> {
    return query.apply(store.list())
}
