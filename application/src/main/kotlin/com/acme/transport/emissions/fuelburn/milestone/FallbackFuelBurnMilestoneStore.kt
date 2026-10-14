package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback fuel burn milestones by code. */
interface FallbackFuelBurnMilestoneStore {
    fun find(code: String): FallbackFuelBurnMilestone?
    fun save(item: FallbackFuelBurnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFuelBurnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFuelBurnMilestoneStore(
    var items: MutableMap<String, FallbackFuelBurnMilestone>,
    var order: MutableList<String>,
) : FallbackFuelBurnMilestoneStore {
    override fun find(code: String): FallbackFuelBurnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFuelBurnMilestone) {
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

    override fun list(): MutableList<FallbackFuelBurnMilestone> {
        val out = mutableListOf<FallbackFuelBurnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFuelBurnMilestoneStore(): FallbackFuelBurnMilestoneStore {
    return MemoryFallbackFuelBurnMilestoneStore(
        mutableMapOf<String, FallbackFuelBurnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFuelBurnMilestoneStore(
    var inner: FallbackFuelBurnMilestoneStore,
    var cache: TtlCache<FallbackFuelBurnMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackFuelBurnMilestoneStore {
    override fun find(code: String): FallbackFuelBurnMilestone? {
        val hit = (cache.get(code) as FallbackFuelBurnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFuelBurnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFuelBurnMilestone> = inner.list()
}

fun newCachedFallbackFuelBurnMilestoneStore(inner: FallbackFuelBurnMilestoneStore, ttl: Int, clock: Clock): FallbackFuelBurnMilestoneStore {
    return CachedFallbackFuelBurnMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackFuelBurnMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback fuel burn milestones. Empty fields match all. */
data class FallbackFuelBurnMilestoneQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFuelBurnMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFuelBurnMilestone>): MutableList<FallbackFuelBurnMilestone> {
        val matched = filter(items) { candidate: FallbackFuelBurnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFuelBurnMilestones(store: FallbackFuelBurnMilestoneStore, query: FallbackFuelBurnMilestoneQuery): MutableList<FallbackFuelBurnMilestone> {
    return query.apply(store.list())
}
