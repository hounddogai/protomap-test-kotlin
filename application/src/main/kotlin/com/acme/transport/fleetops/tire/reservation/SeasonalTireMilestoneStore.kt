package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tire milestones by code. */
interface SeasonalTireMilestoneStore {
    fun find(code: String): SeasonalTireMilestone?
    fun save(item: SeasonalTireMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTireMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTireMilestoneStore(
    var items: MutableMap<String, SeasonalTireMilestone>,
    var order: MutableList<String>,
) : SeasonalTireMilestoneStore {
    override fun find(code: String): SeasonalTireMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTireMilestone) {
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

    override fun list(): MutableList<SeasonalTireMilestone> {
        val out = mutableListOf<SeasonalTireMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTireMilestoneStore(): SeasonalTireMilestoneStore {
    return MemorySeasonalTireMilestoneStore(
        mutableMapOf<String, SeasonalTireMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTireMilestoneStore(
    var inner: SeasonalTireMilestoneStore,
    var cache: TtlCache<SeasonalTireMilestone>,
    var hits: Int,
    var misses: Int,
) : SeasonalTireMilestoneStore {
    override fun find(code: String): SeasonalTireMilestone? {
        val hit = (cache.get(code) as SeasonalTireMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTireMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTireMilestone> = inner.list()
}

fun newCachedSeasonalTireMilestoneStore(inner: SeasonalTireMilestoneStore, ttl: Int, clock: Clock): SeasonalTireMilestoneStore {
    return CachedSeasonalTireMilestoneStore(
        inner = inner,
        cache = newTtlCache<SeasonalTireMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tire milestones. Empty fields match all. */
data class SeasonalTireMilestoneQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTireMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTireMilestone>): MutableList<SeasonalTireMilestone> {
        val matched = filter(items) { candidate: SeasonalTireMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTireMilestones(store: SeasonalTireMilestoneStore, query: SeasonalTireMilestoneQuery): MutableList<SeasonalTireMilestone> {
    return query.apply(store.list())
}
