package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rest break milestones by code. */
interface LegacyRestBreakMilestoneStore {
    fun find(code: String): LegacyRestBreakMilestone?
    fun save(item: LegacyRestBreakMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRestBreakMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRestBreakMilestoneStore(
    var items: MutableMap<String, LegacyRestBreakMilestone>,
    var order: MutableList<String>,
) : LegacyRestBreakMilestoneStore {
    override fun find(code: String): LegacyRestBreakMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRestBreakMilestone) {
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

    override fun list(): MutableList<LegacyRestBreakMilestone> {
        val out = mutableListOf<LegacyRestBreakMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRestBreakMilestoneStore(): LegacyRestBreakMilestoneStore {
    return MemoryLegacyRestBreakMilestoneStore(
        mutableMapOf<String, LegacyRestBreakMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRestBreakMilestoneStore(
    var inner: LegacyRestBreakMilestoneStore,
    var cache: TtlCache<LegacyRestBreakMilestone>,
    var hits: Int,
    var misses: Int,
) : LegacyRestBreakMilestoneStore {
    override fun find(code: String): LegacyRestBreakMilestone? {
        val hit = (cache.get(code) as LegacyRestBreakMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRestBreakMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRestBreakMilestone> = inner.list()
}

fun newCachedLegacyRestBreakMilestoneStore(inner: LegacyRestBreakMilestoneStore, ttl: Int, clock: Clock): LegacyRestBreakMilestoneStore {
    return CachedLegacyRestBreakMilestoneStore(
        inner = inner,
        cache = newTtlCache<LegacyRestBreakMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rest break milestones. Empty fields match all. */
data class LegacyRestBreakMilestoneQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRestBreakMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LegacyRestBreakMilestone>): MutableList<LegacyRestBreakMilestone> {
        val matched = filter(items) { candidate: LegacyRestBreakMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRestBreakMilestones(store: LegacyRestBreakMilestoneStore, query: LegacyRestBreakMilestoneQuery): MutableList<LegacyRestBreakMilestone> {
    return query.apply(store.list())
}
