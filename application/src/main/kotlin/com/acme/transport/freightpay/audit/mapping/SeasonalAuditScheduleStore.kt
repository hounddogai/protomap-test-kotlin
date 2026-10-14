package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal audit schedules by code. */
interface SeasonalAuditScheduleStore {
    fun find(code: String): SeasonalAuditSchedule?
    fun save(item: SeasonalAuditSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAuditSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAuditScheduleStore(
    var items: MutableMap<String, SeasonalAuditSchedule>,
    var order: MutableList<String>,
) : SeasonalAuditScheduleStore {
    override fun find(code: String): SeasonalAuditSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAuditSchedule) {
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

    override fun list(): MutableList<SeasonalAuditSchedule> {
        val out = mutableListOf<SeasonalAuditSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAuditScheduleStore(): SeasonalAuditScheduleStore {
    return MemorySeasonalAuditScheduleStore(
        mutableMapOf<String, SeasonalAuditSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAuditScheduleStore(
    var inner: SeasonalAuditScheduleStore,
    var cache: TtlCache<SeasonalAuditSchedule>,
    var hits: Int,
    var misses: Int,
) : SeasonalAuditScheduleStore {
    override fun find(code: String): SeasonalAuditSchedule? {
        val hit = (cache.get(code) as SeasonalAuditSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAuditSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAuditSchedule> = inner.list()
}

fun newCachedSeasonalAuditScheduleStore(inner: SeasonalAuditScheduleStore, ttl: Int, clock: Clock): SeasonalAuditScheduleStore {
    return CachedSeasonalAuditScheduleStore(
        inner = inner,
        cache = newTtlCache<SeasonalAuditSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal audit schedules. Empty fields match all. */
data class SeasonalAuditScheduleQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAuditSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalAuditSchedule>): MutableList<SeasonalAuditSchedule> {
        val matched = filter(items) { candidate: SeasonalAuditSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAuditSchedules(store: SeasonalAuditScheduleStore, query: SeasonalAuditScheduleQuery): MutableList<SeasonalAuditSchedule> {
    return query.apply(store.list())
}
