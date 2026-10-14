package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rest break schedules by code. */
interface DefaultRestBreakScheduleStore {
    fun find(code: String): DefaultRestBreakSchedule?
    fun save(item: DefaultRestBreakSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRestBreakSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRestBreakScheduleStore(
    var items: MutableMap<String, DefaultRestBreakSchedule>,
    var order: MutableList<String>,
) : DefaultRestBreakScheduleStore {
    override fun find(code: String): DefaultRestBreakSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRestBreakSchedule) {
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

    override fun list(): MutableList<DefaultRestBreakSchedule> {
        val out = mutableListOf<DefaultRestBreakSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRestBreakScheduleStore(): DefaultRestBreakScheduleStore {
    return MemoryDefaultRestBreakScheduleStore(
        mutableMapOf<String, DefaultRestBreakSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRestBreakScheduleStore(
    var inner: DefaultRestBreakScheduleStore,
    var cache: TtlCache<DefaultRestBreakSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultRestBreakScheduleStore {
    override fun find(code: String): DefaultRestBreakSchedule? {
        val hit = (cache.get(code) as DefaultRestBreakSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRestBreakSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRestBreakSchedule> = inner.list()
}

fun newCachedDefaultRestBreakScheduleStore(inner: DefaultRestBreakScheduleStore, ttl: Int, clock: Clock): DefaultRestBreakScheduleStore {
    return CachedDefaultRestBreakScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultRestBreakSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rest break schedules. Empty fields match all. */
data class DefaultRestBreakScheduleQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRestBreakSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRestBreakSchedule>): MutableList<DefaultRestBreakSchedule> {
        val matched = filter(items) { candidate: DefaultRestBreakSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRestBreakSchedules(store: DefaultRestBreakScheduleStore, query: DefaultRestBreakScheduleQuery): MutableList<DefaultRestBreakSchedule> {
    return query.apply(store.list())
}
