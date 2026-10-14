package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered route offset milestones by code. */
interface TieredRouteOffsetMilestoneStore {
    fun find(code: String): TieredRouteOffsetMilestone?
    fun save(item: TieredRouteOffsetMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRouteOffsetMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRouteOffsetMilestoneStore(
    var items: MutableMap<String, TieredRouteOffsetMilestone>,
    var order: MutableList<String>,
) : TieredRouteOffsetMilestoneStore {
    override fun find(code: String): TieredRouteOffsetMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRouteOffsetMilestone) {
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

    override fun list(): MutableList<TieredRouteOffsetMilestone> {
        val out = mutableListOf<TieredRouteOffsetMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRouteOffsetMilestoneStore(): TieredRouteOffsetMilestoneStore {
    return MemoryTieredRouteOffsetMilestoneStore(
        mutableMapOf<String, TieredRouteOffsetMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRouteOffsetMilestoneStore(
    var inner: TieredRouteOffsetMilestoneStore,
    var cache: TtlCache<TieredRouteOffsetMilestone>,
    var hits: Int,
    var misses: Int,
) : TieredRouteOffsetMilestoneStore {
    override fun find(code: String): TieredRouteOffsetMilestone? {
        val hit = (cache.get(code) as TieredRouteOffsetMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRouteOffsetMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRouteOffsetMilestone> = inner.list()
}

fun newCachedTieredRouteOffsetMilestoneStore(inner: TieredRouteOffsetMilestoneStore, ttl: Int, clock: Clock): TieredRouteOffsetMilestoneStore {
    return CachedTieredRouteOffsetMilestoneStore(
        inner = inner,
        cache = newTtlCache<TieredRouteOffsetMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered route offset milestones. Empty fields match all. */
data class TieredRouteOffsetMilestoneQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRouteOffsetMilestone): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRouteOffsetMilestone>): MutableList<TieredRouteOffsetMilestone> {
        val matched = filter(items) { candidate: TieredRouteOffsetMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRouteOffsetMilestones(store: TieredRouteOffsetMilestoneStore, query: TieredRouteOffsetMilestoneQuery): MutableList<TieredRouteOffsetMilestone> {
    return query.apply(store.list())
}
