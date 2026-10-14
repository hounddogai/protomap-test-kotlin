package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal clearance milestones by code. */
interface SeasonalClearanceMilestoneStore {
    fun find(code: String): SeasonalClearanceMilestone?
    fun save(item: SeasonalClearanceMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalClearanceMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalClearanceMilestoneStore(
    var items: MutableMap<String, SeasonalClearanceMilestone>,
    var order: MutableList<String>,
) : SeasonalClearanceMilestoneStore {
    override fun find(code: String): SeasonalClearanceMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalClearanceMilestone) {
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

    override fun list(): MutableList<SeasonalClearanceMilestone> {
        val out = mutableListOf<SeasonalClearanceMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalClearanceMilestoneStore(): SeasonalClearanceMilestoneStore {
    return MemorySeasonalClearanceMilestoneStore(
        mutableMapOf<String, SeasonalClearanceMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalClearanceMilestoneStore(
    var inner: SeasonalClearanceMilestoneStore,
    var cache: TtlCache<SeasonalClearanceMilestone>,
    var hits: Int,
    var misses: Int,
) : SeasonalClearanceMilestoneStore {
    override fun find(code: String): SeasonalClearanceMilestone? {
        val hit = (cache.get(code) as SeasonalClearanceMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalClearanceMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalClearanceMilestone> = inner.list()
}

fun newCachedSeasonalClearanceMilestoneStore(inner: SeasonalClearanceMilestoneStore, ttl: Int, clock: Clock): SeasonalClearanceMilestoneStore {
    return CachedSeasonalClearanceMilestoneStore(
        inner = inner,
        cache = newTtlCache<SeasonalClearanceMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal clearance milestones. Empty fields match all. */
data class SeasonalClearanceMilestoneQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalClearanceMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalClearanceMilestone>): MutableList<SeasonalClearanceMilestone> {
        val matched = filter(items) { candidate: SeasonalClearanceMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalClearanceMilestones(store: SeasonalClearanceMilestoneStore, query: SeasonalClearanceMilestoneQuery): MutableList<SeasonalClearanceMilestone> {
    return query.apply(store.list())
}
