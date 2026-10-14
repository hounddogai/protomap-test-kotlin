package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional redelivery schedules by code. */
interface RegionalRedeliveryScheduleStore {
    fun find(code: String): RegionalRedeliverySchedule?
    fun save(item: RegionalRedeliverySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRedeliverySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRedeliveryScheduleStore(
    var items: MutableMap<String, RegionalRedeliverySchedule>,
    var order: MutableList<String>,
) : RegionalRedeliveryScheduleStore {
    override fun find(code: String): RegionalRedeliverySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRedeliverySchedule) {
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

    override fun list(): MutableList<RegionalRedeliverySchedule> {
        val out = mutableListOf<RegionalRedeliverySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRedeliveryScheduleStore(): RegionalRedeliveryScheduleStore {
    return MemoryRegionalRedeliveryScheduleStore(
        mutableMapOf<String, RegionalRedeliverySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRedeliveryScheduleStore(
    var inner: RegionalRedeliveryScheduleStore,
    var cache: TtlCache<RegionalRedeliverySchedule>,
    var hits: Int,
    var misses: Int,
) : RegionalRedeliveryScheduleStore {
    override fun find(code: String): RegionalRedeliverySchedule? {
        val hit = (cache.get(code) as RegionalRedeliverySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRedeliverySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRedeliverySchedule> = inner.list()
}

fun newCachedRegionalRedeliveryScheduleStore(inner: RegionalRedeliveryScheduleStore, ttl: Int, clock: Clock): RegionalRedeliveryScheduleStore {
    return CachedRegionalRedeliveryScheduleStore(
        inner = inner,
        cache = newTtlCache<RegionalRedeliverySchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional redelivery schedules. Empty fields match all. */
data class RegionalRedeliveryScheduleQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRedeliverySchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRedeliverySchedule>): MutableList<RegionalRedeliverySchedule> {
        val matched = filter(items) { candidate: RegionalRedeliverySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRedeliverySchedules(store: RegionalRedeliveryScheduleStore, query: RegionalRedeliveryScheduleQuery): MutableList<RegionalRedeliverySchedule> {
    return query.apply(store.list())
}
