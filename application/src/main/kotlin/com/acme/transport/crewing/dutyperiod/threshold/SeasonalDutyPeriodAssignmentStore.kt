package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal duty period assignments by code. */
interface SeasonalDutyPeriodAssignmentStore {
    fun find(code: String): SeasonalDutyPeriodAssignment?
    fun save(item: SeasonalDutyPeriodAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDutyPeriodAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDutyPeriodAssignmentStore(
    var items: MutableMap<String, SeasonalDutyPeriodAssignment>,
    var order: MutableList<String>,
) : SeasonalDutyPeriodAssignmentStore {
    override fun find(code: String): SeasonalDutyPeriodAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDutyPeriodAssignment) {
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

    override fun list(): MutableList<SeasonalDutyPeriodAssignment> {
        val out = mutableListOf<SeasonalDutyPeriodAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDutyPeriodAssignmentStore(): SeasonalDutyPeriodAssignmentStore {
    return MemorySeasonalDutyPeriodAssignmentStore(
        mutableMapOf<String, SeasonalDutyPeriodAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDutyPeriodAssignmentStore(
    var inner: SeasonalDutyPeriodAssignmentStore,
    var cache: TtlCache<SeasonalDutyPeriodAssignment>,
    var hits: Int,
    var misses: Int,
) : SeasonalDutyPeriodAssignmentStore {
    override fun find(code: String): SeasonalDutyPeriodAssignment? {
        val hit = (cache.get(code) as SeasonalDutyPeriodAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDutyPeriodAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDutyPeriodAssignment> = inner.list()
}

fun newCachedSeasonalDutyPeriodAssignmentStore(inner: SeasonalDutyPeriodAssignmentStore, ttl: Int, clock: Clock): SeasonalDutyPeriodAssignmentStore {
    return CachedSeasonalDutyPeriodAssignmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalDutyPeriodAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal duty period assignments. Empty fields match all. */
data class SeasonalDutyPeriodAssignmentQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDutyPeriodAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDutyPeriodAssignment>): MutableList<SeasonalDutyPeriodAssignment> {
        val matched = filter(items) { candidate: SeasonalDutyPeriodAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDutyPeriodAssignments(store: SeasonalDutyPeriodAssignmentStore, query: SeasonalDutyPeriodAssignmentQuery): MutableList<SeasonalDutyPeriodAssignment> {
    return query.apply(store.list())
}
