package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending tender plans by code. */
interface PendingTenderPlanStore {
    fun find(code: String): PendingTenderPlan?
    fun save(item: PendingTenderPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTenderPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTenderPlanStore(
    var items: MutableMap<String, PendingTenderPlan>,
    var order: MutableList<String>,
) : PendingTenderPlanStore {
    override fun find(code: String): PendingTenderPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTenderPlan) {
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

    override fun list(): MutableList<PendingTenderPlan> {
        val out = mutableListOf<PendingTenderPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTenderPlanStore(): PendingTenderPlanStore {
    return MemoryPendingTenderPlanStore(mutableMapOf<String, PendingTenderPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTenderPlanStore(
    var inner: PendingTenderPlanStore,
    var cache: TtlCache<PendingTenderPlan>,
    var hits: Int,
    var misses: Int,
) : PendingTenderPlanStore {
    override fun find(code: String): PendingTenderPlan? {
        val hit = (cache.get(code) as PendingTenderPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTenderPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTenderPlan> = inner.list()
}

fun newCachedPendingTenderPlanStore(inner: PendingTenderPlanStore, ttl: Int, clock: Clock): PendingTenderPlanStore {
    return CachedPendingTenderPlanStore(
        inner = inner,
        cache = newTtlCache<PendingTenderPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tender plans. Empty fields match all. */
data class PendingTenderPlanQuery(
    var unit: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTenderPlan): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTenderPlan>): MutableList<PendingTenderPlan> {
        val matched = filter(items) { candidate: PendingTenderPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTenderPlans(store: PendingTenderPlanStore, query: PendingTenderPlanQuery): MutableList<PendingTenderPlan> {
    return query.apply(store.list())
}
