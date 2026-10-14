package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending transit time milestones by code. */
interface PendingTransitTimeMilestoneStore {
    fun find(code: String): PendingTransitTimeMilestone?
    fun save(item: PendingTransitTimeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTransitTimeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTransitTimeMilestoneStore(
    var items: MutableMap<String, PendingTransitTimeMilestone>,
    var order: MutableList<String>,
) : PendingTransitTimeMilestoneStore {
    override fun find(code: String): PendingTransitTimeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTransitTimeMilestone) {
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

    override fun list(): MutableList<PendingTransitTimeMilestone> {
        val out = mutableListOf<PendingTransitTimeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTransitTimeMilestoneStore(): PendingTransitTimeMilestoneStore {
    return MemoryPendingTransitTimeMilestoneStore(
        mutableMapOf<String, PendingTransitTimeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTransitTimeMilestoneStore(
    var inner: PendingTransitTimeMilestoneStore,
    var cache: TtlCache<PendingTransitTimeMilestone>,
    var hits: Int,
    var misses: Int,
) : PendingTransitTimeMilestoneStore {
    override fun find(code: String): PendingTransitTimeMilestone? {
        val hit = (cache.get(code) as PendingTransitTimeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTransitTimeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTransitTimeMilestone> = inner.list()
}

fun newCachedPendingTransitTimeMilestoneStore(inner: PendingTransitTimeMilestoneStore, ttl: Int, clock: Clock): PendingTransitTimeMilestoneStore {
    return CachedPendingTransitTimeMilestoneStore(
        inner = inner,
        cache = newTtlCache<PendingTransitTimeMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending transit time milestones. Empty fields match all. */
data class PendingTransitTimeMilestoneQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTransitTimeMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingTransitTimeMilestone>): MutableList<PendingTransitTimeMilestone> {
        val matched = filter(items) { candidate: PendingTransitTimeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTransitTimeMilestones(store: PendingTransitTimeMilestoneStore, query: PendingTransitTimeMilestoneQuery): MutableList<PendingTransitTimeMilestone> {
    return query.apply(store.list())
}
