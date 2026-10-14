package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal drop milestones by code. */
interface SeasonalDropMilestoneStore {
    fun find(code: String): SeasonalDropMilestone?
    fun save(item: SeasonalDropMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDropMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDropMilestoneStore(
    var items: MutableMap<String, SeasonalDropMilestone>,
    var order: MutableList<String>,
) : SeasonalDropMilestoneStore {
    override fun find(code: String): SeasonalDropMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDropMilestone) {
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

    override fun list(): MutableList<SeasonalDropMilestone> {
        val out = mutableListOf<SeasonalDropMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDropMilestoneStore(): SeasonalDropMilestoneStore {
    return MemorySeasonalDropMilestoneStore(
        mutableMapOf<String, SeasonalDropMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDropMilestoneStore(
    var inner: SeasonalDropMilestoneStore,
    var cache: TtlCache<SeasonalDropMilestone>,
    var hits: Int,
    var misses: Int,
) : SeasonalDropMilestoneStore {
    override fun find(code: String): SeasonalDropMilestone? {
        val hit = (cache.get(code) as SeasonalDropMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDropMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDropMilestone> = inner.list()
}

fun newCachedSeasonalDropMilestoneStore(inner: SeasonalDropMilestoneStore, ttl: Int, clock: Clock): SeasonalDropMilestoneStore {
    return CachedSeasonalDropMilestoneStore(
        inner = inner,
        cache = newTtlCache<SeasonalDropMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal drop milestones. Empty fields match all. */
data class SeasonalDropMilestoneQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDropMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDropMilestone>): MutableList<SeasonalDropMilestone> {
        val matched = filter(items) { candidate: SeasonalDropMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDropMilestones(store: SeasonalDropMilestoneStore, query: SeasonalDropMilestoneQuery): MutableList<SeasonalDropMilestone> {
    return query.apply(store.list())
}
