package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap plans by code. */
interface SwapPlanStore {
    fun find(code: String): SwapPlan?
    fun save(item: SwapPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapPlanStore(
    var items: MutableMap<String, SwapPlan>,
    var order: MutableList<String>,
) : SwapPlanStore {
    override fun find(code: String): SwapPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapPlan) {
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

    override fun list(): MutableList<SwapPlan> {
        val out = mutableListOf<SwapPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapPlanStore(): SwapPlanStore {
    return MemorySwapPlanStore(mutableMapOf<String, SwapPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapPlanStore(
    var inner: SwapPlanStore,
    var cache: TtlCache<SwapPlan>,
    var hits: Int,
    var misses: Int,
) : SwapPlanStore {
    override fun find(code: String): SwapPlan? {
        val hit = (cache.get(code) as SwapPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapPlan> = inner.list()
}

fun newCachedSwapPlanStore(inner: SwapPlanStore, ttl: Int, clock: Clock): SwapPlanStore {
    return CachedSwapPlanStore(
        inner = inner,
        cache = newTtlCache<SwapPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap plans. Empty fields match all. */
data class SwapPlanQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapPlan): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapPlan>): MutableList<SwapPlan> {
        val matched = filter(items) { candidate: SwapPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapPlans(store: SwapPlanStore, query: SwapPlanQuery): MutableList<SwapPlan> = query.apply(store.list())
