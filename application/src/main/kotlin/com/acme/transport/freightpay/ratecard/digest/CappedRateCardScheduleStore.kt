package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rate card schedules by code. */
interface CappedRateCardScheduleStore {
    fun find(code: String): CappedRateCardSchedule?
    fun save(item: CappedRateCardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRateCardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRateCardScheduleStore(
    var items: MutableMap<String, CappedRateCardSchedule>,
    var order: MutableList<String>,
) : CappedRateCardScheduleStore {
    override fun find(code: String): CappedRateCardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRateCardSchedule) {
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

    override fun list(): MutableList<CappedRateCardSchedule> {
        val out = mutableListOf<CappedRateCardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRateCardScheduleStore(): CappedRateCardScheduleStore {
    return MemoryCappedRateCardScheduleStore(
        mutableMapOf<String, CappedRateCardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRateCardScheduleStore(
    var inner: CappedRateCardScheduleStore,
    var cache: TtlCache<CappedRateCardSchedule>,
    var hits: Int,
    var misses: Int,
) : CappedRateCardScheduleStore {
    override fun find(code: String): CappedRateCardSchedule? {
        val hit = (cache.get(code) as CappedRateCardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRateCardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRateCardSchedule> = inner.list()
}

fun newCachedCappedRateCardScheduleStore(inner: CappedRateCardScheduleStore, ttl: Int, clock: Clock): CappedRateCardScheduleStore {
    return CachedCappedRateCardScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedRateCardSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rate card schedules. Empty fields match all. */
data class CappedRateCardScheduleQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRateCardSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedRateCardSchedule>): MutableList<CappedRateCardSchedule> {
        val matched = filter(items) { candidate: CappedRateCardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRateCardSchedules(store: CappedRateCardScheduleStore, query: CappedRateCardScheduleQuery): MutableList<CappedRateCardSchedule> {
    return query.apply(store.list())
}
