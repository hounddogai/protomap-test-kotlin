package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan records by code. */
interface SeasonalServicePlanRecordStore {
    fun find(code: String): SeasonalServicePlanRecord?
    fun save(item: SeasonalServicePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanRecordStore(
    var items: MutableMap<String, SeasonalServicePlanRecord>,
    var order: MutableList<String>,
) : SeasonalServicePlanRecordStore {
    override fun find(code: String): SeasonalServicePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanRecord) {
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

    override fun list(): MutableList<SeasonalServicePlanRecord> {
        val out = mutableListOf<SeasonalServicePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanRecordStore(): SeasonalServicePlanRecordStore {
    return MemorySeasonalServicePlanRecordStore(
        mutableMapOf<String, SeasonalServicePlanRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanRecordStore(
    var inner: SeasonalServicePlanRecordStore,
    var cache: TtlCache<SeasonalServicePlanRecord>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanRecordStore {
    override fun find(code: String): SeasonalServicePlanRecord? {
        val hit = (cache.get(code) as SeasonalServicePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanRecord> = inner.list()
}

fun newCachedSeasonalServicePlanRecordStore(inner: SeasonalServicePlanRecordStore, ttl: Int, clock: Clock): SeasonalServicePlanRecordStore {
    return CachedSeasonalServicePlanRecordStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan records. Empty fields match all. */
data class SeasonalServicePlanRecordQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServicePlanRecord>): MutableList<SeasonalServicePlanRecord> {
        val matched = filter(items) { candidate: SeasonalServicePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanRecords(store: SeasonalServicePlanRecordStore, query: SeasonalServicePlanRecordQuery): MutableList<SeasonalServicePlanRecord> {
    return query.apply(store.list())
}
