package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender plans by code. */
interface TenderPlanStore {
    fun find(code: String): TenderPlan?
    fun save(item: TenderPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderPlanStore(
    var items: MutableMap<String, TenderPlan>,
    var order: MutableList<String>,
) : TenderPlanStore {
    override fun find(code: String): TenderPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderPlan) {
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

    override fun list(): MutableList<TenderPlan> {
        val out = mutableListOf<TenderPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderPlanStore(): TenderPlanStore {
    return MemoryTenderPlanStore(mutableMapOf<String, TenderPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderPlanStore(
    var inner: TenderPlanStore,
    var cache: TtlCache<TenderPlan>,
    var hits: Int,
    var misses: Int,
) : TenderPlanStore {
    override fun find(code: String): TenderPlan? {
        val hit = (cache.get(code) as TenderPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderPlan> = inner.list()
}

fun newCachedTenderPlanStore(inner: TenderPlanStore, ttl: Int, clock: Clock): TenderPlanStore {
    return CachedTenderPlanStore(
        inner = inner,
        cache = newTtlCache<TenderPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender plans. Empty fields match all. */
data class TenderPlanQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderPlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderPlan>): MutableList<TenderPlan> {
        val matched = filter(items) { candidate: TenderPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderPlans(store: TenderPlanStore, query: TenderPlanQuery): MutableList<TenderPlan> {
    return query.apply(store.list())
}
