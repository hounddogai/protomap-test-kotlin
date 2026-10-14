package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time milestones by code. */
interface TransitTimeMilestoneStore {
    fun find(code: String): TransitTimeMilestone?
    fun save(item: TransitTimeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeMilestoneStore(
    var items: MutableMap<String, TransitTimeMilestone>,
    var order: MutableList<String>,
) : TransitTimeMilestoneStore {
    override fun find(code: String): TransitTimeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeMilestone) {
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

    override fun list(): MutableList<TransitTimeMilestone> {
        val out = mutableListOf<TransitTimeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeMilestoneStore(): TransitTimeMilestoneStore {
    return MemoryTransitTimeMilestoneStore(
        mutableMapOf<String, TransitTimeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeMilestoneStore(
    var inner: TransitTimeMilestoneStore,
    var cache: TtlCache<TransitTimeMilestone>,
    var hits: Int,
    var misses: Int,
) : TransitTimeMilestoneStore {
    override fun find(code: String): TransitTimeMilestone? {
        val hit = (cache.get(code) as TransitTimeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeMilestone> = inner.list()
}

fun newCachedTransitTimeMilestoneStore(inner: TransitTimeMilestoneStore, ttl: Int, clock: Clock): TransitTimeMilestoneStore {
    return CachedTransitTimeMilestoneStore(
        inner = inner,
        cache = newTtlCache<TransitTimeMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time milestones. Empty fields match all. */
data class TransitTimeMilestoneQuery(
    var category: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeMilestone): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TransitTimeMilestone>): MutableList<TransitTimeMilestone> {
        val matched = filter(items) { candidate: TransitTimeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeMilestones(store: TransitTimeMilestoneStore, query: TransitTimeMilestoneQuery): MutableList<TransitTimeMilestone> {
    return query.apply(store.list())
}
