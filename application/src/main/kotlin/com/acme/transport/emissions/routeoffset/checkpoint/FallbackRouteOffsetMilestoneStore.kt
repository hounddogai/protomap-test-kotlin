package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback route offset milestones by code. */
interface FallbackRouteOffsetMilestoneStore {
    fun find(code: String): FallbackRouteOffsetMilestone?
    fun save(item: FallbackRouteOffsetMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRouteOffsetMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRouteOffsetMilestoneStore(
    var items: MutableMap<String, FallbackRouteOffsetMilestone>,
    var order: MutableList<String>,
) : FallbackRouteOffsetMilestoneStore {
    override fun find(code: String): FallbackRouteOffsetMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRouteOffsetMilestone) {
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

    override fun list(): MutableList<FallbackRouteOffsetMilestone> {
        val out = mutableListOf<FallbackRouteOffsetMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRouteOffsetMilestoneStore(): FallbackRouteOffsetMilestoneStore {
    return MemoryFallbackRouteOffsetMilestoneStore(
        mutableMapOf<String, FallbackRouteOffsetMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRouteOffsetMilestoneStore(
    var inner: FallbackRouteOffsetMilestoneStore,
    var cache: TtlCache<FallbackRouteOffsetMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackRouteOffsetMilestoneStore {
    override fun find(code: String): FallbackRouteOffsetMilestone? {
        val hit = (cache.get(code) as FallbackRouteOffsetMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRouteOffsetMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRouteOffsetMilestone> = inner.list()
}

fun newCachedFallbackRouteOffsetMilestoneStore(inner: FallbackRouteOffsetMilestoneStore, ttl: Int, clock: Clock): FallbackRouteOffsetMilestoneStore {
    return CachedFallbackRouteOffsetMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackRouteOffsetMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback route offset milestones. Empty fields match all. */
data class FallbackRouteOffsetMilestoneQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRouteOffsetMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<FallbackRouteOffsetMilestone>): MutableList<FallbackRouteOffsetMilestone> {
        val matched = filter(items) { candidate: FallbackRouteOffsetMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRouteOffsetMilestones(store: FallbackRouteOffsetMilestoneStore, query: FallbackRouteOffsetMilestoneQuery): MutableList<FallbackRouteOffsetMilestone> {
    return query.apply(store.list())
}
