package com.acme.transport.crewing.swap.audit

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap milestones by code. */
interface SwapMilestoneStore {
    fun find(code: String): SwapMilestone?
    fun save(item: SwapMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapMilestoneStore(
    var items: MutableMap<String, SwapMilestone>,
    var order: MutableList<String>,
) : SwapMilestoneStore {
    override fun find(code: String): SwapMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapMilestone) {
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

    override fun list(): MutableList<SwapMilestone> {
        val out = mutableListOf<SwapMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapMilestoneStore(): SwapMilestoneStore {
    return MemorySwapMilestoneStore(mutableMapOf<String, SwapMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapMilestoneStore(
    var inner: SwapMilestoneStore,
    var cache: TtlCache<SwapMilestone>,
    var hits: Int,
    var misses: Int,
) : SwapMilestoneStore {
    override fun find(code: String): SwapMilestone? {
        val hit = (cache.get(code) as SwapMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapMilestone> = inner.list()
}

fun newCachedSwapMilestoneStore(inner: SwapMilestoneStore, ttl: Int, clock: Clock): SwapMilestoneStore {
    return CachedSwapMilestoneStore(
        inner = inner,
        cache = newTtlCache<SwapMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap milestones. Empty fields match all. */
data class SwapMilestoneQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapMilestone>): MutableList<SwapMilestone> {
        val matched = filter(items) { candidate: SwapMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapMilestones(store: SwapMilestoneStore, query: SwapMilestoneQuery): MutableList<SwapMilestone> {
    return query.apply(store.list())
}
