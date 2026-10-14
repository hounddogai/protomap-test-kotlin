package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop schedules by code. */
interface DropScheduleStore {
    fun find(code: String): DropSchedule?
    fun save(item: DropSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropScheduleStore(
    var items: MutableMap<String, DropSchedule>,
    var order: MutableList<String>,
) : DropScheduleStore {
    override fun find(code: String): DropSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropSchedule) {
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

    override fun list(): MutableList<DropSchedule> {
        val out = mutableListOf<DropSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropScheduleStore(): DropScheduleStore {
    return MemoryDropScheduleStore(mutableMapOf<String, DropSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropScheduleStore(
    var inner: DropScheduleStore,
    var cache: TtlCache<DropSchedule>,
    var hits: Int,
    var misses: Int,
) : DropScheduleStore {
    override fun find(code: String): DropSchedule? {
        val hit = (cache.get(code) as DropSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropSchedule> = inner.list()
}

fun newCachedDropScheduleStore(inner: DropScheduleStore, ttl: Int, clock: Clock): DropScheduleStore {
    return CachedDropScheduleStore(
        inner = inner,
        cache = newTtlCache<DropSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop schedules. Empty fields match all. */
data class DropScheduleQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DropSchedule>): MutableList<DropSchedule> {
        val matched = filter(items) { candidate: DropSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropSchedules(store: DropScheduleStore, query: DropScheduleQuery): MutableList<DropSchedule> {
    return query.apply(store.list())
}
