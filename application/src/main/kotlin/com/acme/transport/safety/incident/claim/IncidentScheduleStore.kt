package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident schedules by code. */
interface IncidentScheduleStore {
    fun find(code: String): IncidentSchedule?
    fun save(item: IncidentSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentScheduleStore(
    var items: MutableMap<String, IncidentSchedule>,
    var order: MutableList<String>,
) : IncidentScheduleStore {
    override fun find(code: String): IncidentSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentSchedule) {
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

    override fun list(): MutableList<IncidentSchedule> {
        val out = mutableListOf<IncidentSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentScheduleStore(): IncidentScheduleStore {
    return MemoryIncidentScheduleStore(mutableMapOf<String, IncidentSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentScheduleStore(
    var inner: IncidentScheduleStore,
    var cache: TtlCache<IncidentSchedule>,
    var hits: Int,
    var misses: Int,
) : IncidentScheduleStore {
    override fun find(code: String): IncidentSchedule? {
        val hit = (cache.get(code) as IncidentSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentSchedule> = inner.list()
}

fun newCachedIncidentScheduleStore(inner: IncidentScheduleStore, ttl: Int, clock: Clock): IncidentScheduleStore {
    return CachedIncidentScheduleStore(
        inner = inner,
        cache = newTtlCache<IncidentSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident schedules. Empty fields match all. */
data class IncidentScheduleQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<IncidentSchedule>): MutableList<IncidentSchedule> {
        val matched = filter(items) { candidate: IncidentSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentSchedules(store: IncidentScheduleStore, query: IncidentScheduleQuery): MutableList<IncidentSchedule> {
    return query.apply(store.list())
}
