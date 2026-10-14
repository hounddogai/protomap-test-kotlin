package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card schedules by code. */
interface RateCardScheduleStore {
    fun find(code: String): RateCardSchedule?
    fun save(item: RateCardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardScheduleStore(
    var items: MutableMap<String, RateCardSchedule>,
    var order: MutableList<String>,
) : RateCardScheduleStore {
    override fun find(code: String): RateCardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardSchedule) {
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

    override fun list(): MutableList<RateCardSchedule> {
        val out = mutableListOf<RateCardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardScheduleStore(): RateCardScheduleStore {
    return MemoryRateCardScheduleStore(mutableMapOf<String, RateCardSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardScheduleStore(
    var inner: RateCardScheduleStore,
    var cache: TtlCache<RateCardSchedule>,
    var hits: Int,
    var misses: Int,
) : RateCardScheduleStore {
    override fun find(code: String): RateCardSchedule? {
        val hit = (cache.get(code) as RateCardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardSchedule> = inner.list()
}

fun newCachedRateCardScheduleStore(inner: RateCardScheduleStore, ttl: Int, clock: Clock): RateCardScheduleStore {
    return CachedRateCardScheduleStore(
        inner = inner,
        cache = newTtlCache<RateCardSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card schedules. Empty fields match all. */
data class RateCardScheduleQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardSchedule>): MutableList<RateCardSchedule> {
        val matched = filter(items) { candidate: RateCardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardSchedules(store: RateCardScheduleStore, query: RateCardScheduleQuery): MutableList<RateCardSchedule> {
    return query.apply(store.list())
}
