package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal scorecard assignments by code. */
interface SeasonalScorecardAssignmentStore {
    fun find(code: String): SeasonalScorecardAssignment?
    fun save(item: SeasonalScorecardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalScorecardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalScorecardAssignmentStore(
    var items: MutableMap<String, SeasonalScorecardAssignment>,
    var order: MutableList<String>,
) : SeasonalScorecardAssignmentStore {
    override fun find(code: String): SeasonalScorecardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalScorecardAssignment) {
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

    override fun list(): MutableList<SeasonalScorecardAssignment> {
        val out = mutableListOf<SeasonalScorecardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalScorecardAssignmentStore(): SeasonalScorecardAssignmentStore {
    return MemorySeasonalScorecardAssignmentStore(
        mutableMapOf<String, SeasonalScorecardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalScorecardAssignmentStore(
    var inner: SeasonalScorecardAssignmentStore,
    var cache: TtlCache<SeasonalScorecardAssignment>,
    var hits: Int,
    var misses: Int,
) : SeasonalScorecardAssignmentStore {
    override fun find(code: String): SeasonalScorecardAssignment? {
        val hit = (cache.get(code) as SeasonalScorecardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalScorecardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalScorecardAssignment> = inner.list()
}

fun newCachedSeasonalScorecardAssignmentStore(inner: SeasonalScorecardAssignmentStore, ttl: Int, clock: Clock): SeasonalScorecardAssignmentStore {
    return CachedSeasonalScorecardAssignmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalScorecardAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal scorecard assignments. Empty fields match all. */
data class SeasonalScorecardAssignmentQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalScorecardAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalScorecardAssignment>): MutableList<SeasonalScorecardAssignment> {
        val matched = filter(items) { candidate: SeasonalScorecardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalScorecardAssignments(store: SeasonalScorecardAssignmentStore, query: SeasonalScorecardAssignmentQuery): MutableList<SeasonalScorecardAssignment> {
    return query.apply(store.list())
}
