package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary availability plans by code. */
interface PrimaryAvailabilityPlanStore {
    fun find(code: String): PrimaryAvailabilityPlan?
    fun save(item: PrimaryAvailabilityPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryAvailabilityPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryAvailabilityPlanStore(
    var items: MutableMap<String, PrimaryAvailabilityPlan>,
    var order: MutableList<String>,
) : PrimaryAvailabilityPlanStore {
    override fun find(code: String): PrimaryAvailabilityPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryAvailabilityPlan) {
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

    override fun list(): MutableList<PrimaryAvailabilityPlan> {
        val out = mutableListOf<PrimaryAvailabilityPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryAvailabilityPlanStore(): PrimaryAvailabilityPlanStore {
    return MemoryPrimaryAvailabilityPlanStore(
        mutableMapOf<String, PrimaryAvailabilityPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryAvailabilityPlanStore(
    var inner: PrimaryAvailabilityPlanStore,
    var cache: TtlCache<PrimaryAvailabilityPlan>,
    var hits: Int,
    var misses: Int,
) : PrimaryAvailabilityPlanStore {
    override fun find(code: String): PrimaryAvailabilityPlan? {
        val hit = (cache.get(code) as PrimaryAvailabilityPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryAvailabilityPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryAvailabilityPlan> = inner.list()
}

fun newCachedPrimaryAvailabilityPlanStore(inner: PrimaryAvailabilityPlanStore, ttl: Int, clock: Clock): PrimaryAvailabilityPlanStore {
    return CachedPrimaryAvailabilityPlanStore(
        inner = inner,
        cache = newTtlCache<PrimaryAvailabilityPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary availability plans. Empty fields match all. */
data class PrimaryAvailabilityPlanQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryAvailabilityPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryAvailabilityPlan>): MutableList<PrimaryAvailabilityPlan> {
        val matched = filter(items) { candidate: PrimaryAvailabilityPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryAvailabilityPlans(store: PrimaryAvailabilityPlanStore, query: PrimaryAvailabilityPlanQuery): MutableList<PrimaryAvailabilityPlan> {
    return query.apply(store.list())
}
