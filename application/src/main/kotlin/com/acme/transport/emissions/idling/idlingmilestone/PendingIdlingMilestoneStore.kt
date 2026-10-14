package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending idling milestones by code. */
interface PendingIdlingMilestoneStore {
    fun find(code: String): PendingIdlingMilestone?
    fun save(item: PendingIdlingMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingIdlingMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingIdlingMilestoneStore(
    var items: MutableMap<String, PendingIdlingMilestone>,
    var order: MutableList<String>,
) : PendingIdlingMilestoneStore {
    override fun find(code: String): PendingIdlingMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingIdlingMilestone) {
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

    override fun list(): MutableList<PendingIdlingMilestone> {
        val out = mutableListOf<PendingIdlingMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingIdlingMilestoneStore(): PendingIdlingMilestoneStore {
    return MemoryPendingIdlingMilestoneStore(
        mutableMapOf<String, PendingIdlingMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingIdlingMilestoneStore(
    var inner: PendingIdlingMilestoneStore,
    var cache: TtlCache<PendingIdlingMilestone>,
    var hits: Int,
    var misses: Int,
) : PendingIdlingMilestoneStore {
    override fun find(code: String): PendingIdlingMilestone? {
        val hit = (cache.get(code) as PendingIdlingMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingIdlingMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingIdlingMilestone> = inner.list()
}

fun newCachedPendingIdlingMilestoneStore(inner: PendingIdlingMilestoneStore, ttl: Int, clock: Clock): PendingIdlingMilestoneStore {
    return CachedPendingIdlingMilestoneStore(
        inner = inner,
        cache = newTtlCache<PendingIdlingMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending idling milestones. Empty fields match all. */
data class PendingIdlingMilestoneQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingIdlingMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingIdlingMilestone>): MutableList<PendingIdlingMilestone> {
        val matched = filter(items) { candidate: PendingIdlingMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingIdlingMilestones(store: PendingIdlingMilestoneStore, query: PendingIdlingMilestoneQuery): MutableList<PendingIdlingMilestone> {
    return query.apply(store.list())
}
