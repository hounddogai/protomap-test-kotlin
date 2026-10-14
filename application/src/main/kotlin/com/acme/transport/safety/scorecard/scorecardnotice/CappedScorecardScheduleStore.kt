package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped scorecard schedules by code. */
interface CappedScorecardScheduleStore {
    fun find(code: String): CappedScorecardSchedule?
    fun save(item: CappedScorecardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedScorecardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedScorecardScheduleStore(
    var items: MutableMap<String, CappedScorecardSchedule>,
    var order: MutableList<String>,
) : CappedScorecardScheduleStore {
    override fun find(code: String): CappedScorecardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedScorecardSchedule) {
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

    override fun list(): MutableList<CappedScorecardSchedule> {
        val out = mutableListOf<CappedScorecardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedScorecardScheduleStore(): CappedScorecardScheduleStore {
    return MemoryCappedScorecardScheduleStore(
        mutableMapOf<String, CappedScorecardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedScorecardScheduleStore(
    var inner: CappedScorecardScheduleStore,
    var cache: TtlCache<CappedScorecardSchedule>,
    var hits: Int,
    var misses: Int,
) : CappedScorecardScheduleStore {
    override fun find(code: String): CappedScorecardSchedule? {
        val hit = (cache.get(code) as CappedScorecardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedScorecardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedScorecardSchedule> = inner.list()
}

fun newCachedCappedScorecardScheduleStore(inner: CappedScorecardScheduleStore, ttl: Int, clock: Clock): CappedScorecardScheduleStore {
    return CachedCappedScorecardScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedScorecardSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped scorecard schedules. Empty fields match all. */
data class CappedScorecardScheduleQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedScorecardSchedule): Boolean {
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

    fun apply(items: MutableList<CappedScorecardSchedule>): MutableList<CappedScorecardSchedule> {
        val matched = filter(items) { candidate: CappedScorecardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedScorecardSchedules(store: CappedScorecardScheduleStore, query: CappedScorecardScheduleQuery): MutableList<CappedScorecardSchedule> {
    return query.apply(store.list())
}
