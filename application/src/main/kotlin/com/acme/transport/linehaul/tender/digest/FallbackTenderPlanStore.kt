package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback tender plans by code. */
interface FallbackTenderPlanStore {
    fun find(code: String): FallbackTenderPlan?
    fun save(item: FallbackTenderPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTenderPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTenderPlanStore(
    var items: MutableMap<String, FallbackTenderPlan>,
    var order: MutableList<String>,
) : FallbackTenderPlanStore {
    override fun find(code: String): FallbackTenderPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTenderPlan) {
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

    override fun list(): MutableList<FallbackTenderPlan> {
        val out = mutableListOf<FallbackTenderPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTenderPlanStore(): FallbackTenderPlanStore {
    return MemoryFallbackTenderPlanStore(mutableMapOf<String, FallbackTenderPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTenderPlanStore(
    var inner: FallbackTenderPlanStore,
    var cache: TtlCache<FallbackTenderPlan>,
    var hits: Int,
    var misses: Int,
) : FallbackTenderPlanStore {
    override fun find(code: String): FallbackTenderPlan? {
        val hit = (cache.get(code) as FallbackTenderPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTenderPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTenderPlan> = inner.list()
}

fun newCachedFallbackTenderPlanStore(inner: FallbackTenderPlanStore, ttl: Int, clock: Clock): FallbackTenderPlanStore {
    return CachedFallbackTenderPlanStore(
        inner = inner,
        cache = newTtlCache<FallbackTenderPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tender plans. Empty fields match all. */
data class FallbackTenderPlanQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTenderPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTenderPlan>): MutableList<FallbackTenderPlan> {
        val matched = filter(items) { candidate: FallbackTenderPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTenderPlans(store: FallbackTenderPlanStore, query: FallbackTenderPlanQuery): MutableList<FallbackTenderPlan> {
    return query.apply(store.list())
}
