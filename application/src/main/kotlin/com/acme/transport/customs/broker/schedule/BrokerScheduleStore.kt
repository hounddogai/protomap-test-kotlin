package com.acme.transport.customs.broker.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker schedules by code. */
interface BrokerScheduleStore {
    fun find(code: String): BrokerSchedule?
    fun save(item: BrokerSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerScheduleStore(
    var items: MutableMap<String, BrokerSchedule>,
    var order: MutableList<String>,
) : BrokerScheduleStore {
    override fun find(code: String): BrokerSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerSchedule) {
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

    override fun list(): MutableList<BrokerSchedule> {
        val out = mutableListOf<BrokerSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerScheduleStore(): BrokerScheduleStore {
    return MemoryBrokerScheduleStore(mutableMapOf<String, BrokerSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerScheduleStore(
    var inner: BrokerScheduleStore,
    var cache: TtlCache<BrokerSchedule>,
    var hits: Int,
    var misses: Int,
) : BrokerScheduleStore {
    override fun find(code: String): BrokerSchedule? {
        val hit = (cache.get(code) as BrokerSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerSchedule> = inner.list()
}

fun newCachedBrokerScheduleStore(inner: BrokerScheduleStore, ttl: Int, clock: Clock): BrokerScheduleStore {
    return CachedBrokerScheduleStore(
        inner = inner,
        cache = newTtlCache<BrokerSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker schedules. Empty fields match all. */
data class BrokerScheduleQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerSchedule>): MutableList<BrokerSchedule> {
        val matched = filter(items) { candidate: BrokerSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerSchedules(store: BrokerScheduleStore, query: BrokerScheduleQuery): MutableList<BrokerSchedule> {
    return query.apply(store.list())
}
