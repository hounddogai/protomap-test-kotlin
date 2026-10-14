package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict zone schedules by code. */
interface StrictZoneScheduleStore {
    fun find(code: String): StrictZoneSchedule?
    fun save(item: StrictZoneSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictZoneSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictZoneScheduleStore(
    var items: MutableMap<String, StrictZoneSchedule>,
    var order: MutableList<String>,
) : StrictZoneScheduleStore {
    override fun find(code: String): StrictZoneSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictZoneSchedule) {
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

    override fun list(): MutableList<StrictZoneSchedule> {
        val out = mutableListOf<StrictZoneSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictZoneScheduleStore(): StrictZoneScheduleStore {
    return MemoryStrictZoneScheduleStore(mutableMapOf<String, StrictZoneSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictZoneScheduleStore(
    var inner: StrictZoneScheduleStore,
    var cache: TtlCache<StrictZoneSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictZoneScheduleStore {
    override fun find(code: String): StrictZoneSchedule? {
        val hit = (cache.get(code) as StrictZoneSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictZoneSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictZoneSchedule> = inner.list()
}

fun newCachedStrictZoneScheduleStore(inner: StrictZoneScheduleStore, ttl: Int, clock: Clock): StrictZoneScheduleStore {
    return CachedStrictZoneScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictZoneSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict zone schedules. Empty fields match all. */
data class StrictZoneScheduleQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictZoneSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictZoneSchedule>): MutableList<StrictZoneSchedule> {
        val matched = filter(items) { candidate: StrictZoneSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictZoneSchedules(store: StrictZoneScheduleStore, query: StrictZoneScheduleQuery): MutableList<StrictZoneSchedule> {
    return query.apply(store.list())
}
