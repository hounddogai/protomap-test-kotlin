package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled inspection scores by code. */
interface ScheduledInspectionScoreStore {
    fun find(code: String): ScheduledInspectionScore?
    fun save(item: ScheduledInspectionScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledInspectionScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledInspectionScoreStore(
    var items: MutableMap<String, ScheduledInspectionScore>,
    var order: MutableList<String>,
) : ScheduledInspectionScoreStore {
    override fun find(code: String): ScheduledInspectionScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledInspectionScore) {
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

    override fun list(): MutableList<ScheduledInspectionScore> {
        val out = mutableListOf<ScheduledInspectionScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledInspectionScoreStore(): ScheduledInspectionScoreStore {
    return MemoryScheduledInspectionScoreStore(
        mutableMapOf<String, ScheduledInspectionScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledInspectionScoreStore(
    var inner: ScheduledInspectionScoreStore,
    var cache: TtlCache<ScheduledInspectionScore>,
    var hits: Int,
    var misses: Int,
) : ScheduledInspectionScoreStore {
    override fun find(code: String): ScheduledInspectionScore? {
        val hit = (cache.get(code) as ScheduledInspectionScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledInspectionScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledInspectionScore> = inner.list()
}

fun newCachedScheduledInspectionScoreStore(inner: ScheduledInspectionScoreStore, ttl: Int, clock: Clock): ScheduledInspectionScoreStore {
    return CachedScheduledInspectionScoreStore(
        inner = inner,
        cache = newTtlCache<ScheduledInspectionScore>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled inspection scores. Empty fields match all. */
data class ScheduledInspectionScoreQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledInspectionScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledInspectionScore>): MutableList<ScheduledInspectionScore> {
        val matched = filter(items) { candidate: ScheduledInspectionScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledInspectionScores(store: ScheduledInspectionScoreStore, query: ScheduledInspectionScoreQuery): MutableList<ScheduledInspectionScore> {
    return query.apply(store.list())
}
