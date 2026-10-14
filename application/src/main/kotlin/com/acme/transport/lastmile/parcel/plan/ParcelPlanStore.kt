package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel plans by code. */
interface ParcelPlanStore {
    fun find(code: String): ParcelPlan?
    fun save(item: ParcelPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelPlanStore(
    var items: MutableMap<String, ParcelPlan>,
    var order: MutableList<String>,
) : ParcelPlanStore {
    override fun find(code: String): ParcelPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelPlan) {
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

    override fun list(): MutableList<ParcelPlan> {
        val out = mutableListOf<ParcelPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelPlanStore(): ParcelPlanStore {
    return MemoryParcelPlanStore(mutableMapOf<String, ParcelPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelPlanStore(
    var inner: ParcelPlanStore,
    var cache: TtlCache<ParcelPlan>,
    var hits: Int,
    var misses: Int,
) : ParcelPlanStore {
    override fun find(code: String): ParcelPlan? {
        val hit = (cache.get(code) as ParcelPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelPlan> = inner.list()
}

fun newCachedParcelPlanStore(inner: ParcelPlanStore, ttl: Int, clock: Clock): ParcelPlanStore {
    return CachedParcelPlanStore(
        inner = inner,
        cache = newTtlCache<ParcelPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel plans. Empty fields match all. */
data class ParcelPlanQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelPlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelPlan>): MutableList<ParcelPlan> {
        val matched = filter(items) { candidate: ParcelPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelPlans(store: ParcelPlanStore, query: ParcelPlanQuery): MutableList<ParcelPlan> {
    return query.apply(store.list())
}
