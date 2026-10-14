package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict near miss schedules by code. */
interface StrictNearMissScheduleStore {
    fun find(code: String): StrictNearMissSchedule?
    fun save(item: StrictNearMissSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNearMissSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNearMissScheduleStore(
    var items: MutableMap<String, StrictNearMissSchedule>,
    var order: MutableList<String>,
) : StrictNearMissScheduleStore {
    override fun find(code: String): StrictNearMissSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNearMissSchedule) {
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

    override fun list(): MutableList<StrictNearMissSchedule> {
        val out = mutableListOf<StrictNearMissSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNearMissScheduleStore(): StrictNearMissScheduleStore {
    return MemoryStrictNearMissScheduleStore(
        mutableMapOf<String, StrictNearMissSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNearMissScheduleStore(
    var inner: StrictNearMissScheduleStore,
    var cache: TtlCache<StrictNearMissSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictNearMissScheduleStore {
    override fun find(code: String): StrictNearMissSchedule? {
        val hit = (cache.get(code) as StrictNearMissSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNearMissSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNearMissSchedule> = inner.list()
}

fun newCachedStrictNearMissScheduleStore(inner: StrictNearMissScheduleStore, ttl: Int, clock: Clock): StrictNearMissScheduleStore {
    return CachedStrictNearMissScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictNearMissSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict near miss schedules. Empty fields match all. */
data class StrictNearMissScheduleQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNearMissSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNearMissSchedule>): MutableList<StrictNearMissSchedule> {
        val matched = filter(items) { candidate: StrictNearMissSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNearMissSchedules(store: StrictNearMissScheduleStore, query: StrictNearMissScheduleQuery): MutableList<StrictNearMissSchedule> {
    return query.apply(store.list())
}
