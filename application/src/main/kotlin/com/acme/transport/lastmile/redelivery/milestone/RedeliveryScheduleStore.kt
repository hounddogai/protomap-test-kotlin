package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery schedules by code. */
interface RedeliveryScheduleStore {
    fun find(code: String): RedeliverySchedule?
    fun save(item: RedeliverySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliverySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryScheduleStore(
    var items: MutableMap<String, RedeliverySchedule>,
    var order: MutableList<String>,
) : RedeliveryScheduleStore {
    override fun find(code: String): RedeliverySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliverySchedule) {
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

    override fun list(): MutableList<RedeliverySchedule> {
        val out = mutableListOf<RedeliverySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryScheduleStore(): RedeliveryScheduleStore {
    return MemoryRedeliveryScheduleStore(mutableMapOf<String, RedeliverySchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryScheduleStore(
    var inner: RedeliveryScheduleStore,
    var cache: TtlCache<RedeliverySchedule>,
    var hits: Int,
    var misses: Int,
) : RedeliveryScheduleStore {
    override fun find(code: String): RedeliverySchedule? {
        val hit = (cache.get(code) as RedeliverySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliverySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliverySchedule> = inner.list()
}

fun newCachedRedeliveryScheduleStore(inner: RedeliveryScheduleStore, ttl: Int, clock: Clock): RedeliveryScheduleStore {
    return CachedRedeliveryScheduleStore(
        inner = inner,
        cache = newTtlCache<RedeliverySchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery schedules. Empty fields match all. */
data class RedeliveryScheduleQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliverySchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliverySchedule>): MutableList<RedeliverySchedule> {
        val matched = filter(items) { candidate: RedeliverySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliverySchedules(store: RedeliveryScheduleStore, query: RedeliveryScheduleQuery): MutableList<RedeliverySchedule> {
    return query.apply(store.list())
}
