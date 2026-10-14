package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rate card schedules by code. */
interface RollingRateCardScheduleStore {
    fun find(code: String): RollingRateCardSchedule?
    fun save(item: RollingRateCardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRateCardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRateCardScheduleStore(
    var items: MutableMap<String, RollingRateCardSchedule>,
    var order: MutableList<String>,
) : RollingRateCardScheduleStore {
    override fun find(code: String): RollingRateCardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRateCardSchedule) {
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

    override fun list(): MutableList<RollingRateCardSchedule> {
        val out = mutableListOf<RollingRateCardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRateCardScheduleStore(): RollingRateCardScheduleStore {
    return MemoryRollingRateCardScheduleStore(
        mutableMapOf<String, RollingRateCardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRateCardScheduleStore(
    var inner: RollingRateCardScheduleStore,
    var cache: TtlCache<RollingRateCardSchedule>,
    var hits: Int,
    var misses: Int,
) : RollingRateCardScheduleStore {
    override fun find(code: String): RollingRateCardSchedule? {
        val hit = (cache.get(code) as RollingRateCardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRateCardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRateCardSchedule> = inner.list()
}

fun newCachedRollingRateCardScheduleStore(inner: RollingRateCardScheduleStore, ttl: Int, clock: Clock): RollingRateCardScheduleStore {
    return CachedRollingRateCardScheduleStore(
        inner = inner,
        cache = newTtlCache<RollingRateCardSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rate card schedules. Empty fields match all. */
data class RollingRateCardScheduleQuery(
    var locale: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRateCardSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRateCardSchedule>): MutableList<RollingRateCardSchedule> {
        val matched = filter(items) { candidate: RollingRateCardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRateCardSchedules(store: RollingRateCardScheduleStore, query: RollingRateCardScheduleQuery): MutableList<RollingRateCardSchedule> {
    return query.apply(store.list())
}
