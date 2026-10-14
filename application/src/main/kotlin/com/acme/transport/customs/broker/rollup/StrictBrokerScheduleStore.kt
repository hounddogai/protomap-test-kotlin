package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict broker schedules by code. */
interface StrictBrokerScheduleStore {
    fun find(code: String): StrictBrokerSchedule?
    fun save(item: StrictBrokerSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBrokerSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBrokerScheduleStore(
    var items: MutableMap<String, StrictBrokerSchedule>,
    var order: MutableList<String>,
) : StrictBrokerScheduleStore {
    override fun find(code: String): StrictBrokerSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBrokerSchedule) {
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

    override fun list(): MutableList<StrictBrokerSchedule> {
        val out = mutableListOf<StrictBrokerSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBrokerScheduleStore(): StrictBrokerScheduleStore {
    return MemoryStrictBrokerScheduleStore(
        mutableMapOf<String, StrictBrokerSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBrokerScheduleStore(
    var inner: StrictBrokerScheduleStore,
    var cache: TtlCache<StrictBrokerSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictBrokerScheduleStore {
    override fun find(code: String): StrictBrokerSchedule? {
        val hit = (cache.get(code) as StrictBrokerSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBrokerSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBrokerSchedule> = inner.list()
}

fun newCachedStrictBrokerScheduleStore(inner: StrictBrokerScheduleStore, ttl: Int, clock: Clock): StrictBrokerScheduleStore {
    return CachedStrictBrokerScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictBrokerSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict broker schedules. Empty fields match all. */
data class StrictBrokerScheduleQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBrokerSchedule): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBrokerSchedule>): MutableList<StrictBrokerSchedule> {
        val matched = filter(items) { candidate: StrictBrokerSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBrokerSchedules(store: StrictBrokerScheduleStore, query: StrictBrokerScheduleQuery): MutableList<StrictBrokerSchedule> {
    return query.apply(store.list())
}
