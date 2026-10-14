package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan schedules by code. */
interface LanePlanScheduleStore {
    fun find(code: String): LanePlanSchedule?
    fun save(item: LanePlanSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanScheduleStore(
    var items: MutableMap<String, LanePlanSchedule>,
    var order: MutableList<String>,
) : LanePlanScheduleStore {
    override fun find(code: String): LanePlanSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanSchedule) {
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

    override fun list(): MutableList<LanePlanSchedule> {
        val out = mutableListOf<LanePlanSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanScheduleStore(): LanePlanScheduleStore {
    return MemoryLanePlanScheduleStore(mutableMapOf<String, LanePlanSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanScheduleStore(
    var inner: LanePlanScheduleStore,
    var cache: TtlCache<LanePlanSchedule>,
    var hits: Int,
    var misses: Int,
) : LanePlanScheduleStore {
    override fun find(code: String): LanePlanSchedule? {
        val hit = (cache.get(code) as LanePlanSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanSchedule> = inner.list()
}

fun newCachedLanePlanScheduleStore(inner: LanePlanScheduleStore, ttl: Int, clock: Clock): LanePlanScheduleStore {
    return CachedLanePlanScheduleStore(
        inner = inner,
        cache = newTtlCache<LanePlanSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan schedules. Empty fields match all. */
data class LanePlanScheduleQuery(
    var channel: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanSchedule>): MutableList<LanePlanSchedule> {
        val matched = filter(items) { candidate: LanePlanSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanSchedules(store: LanePlanScheduleStore, query: LanePlanScheduleQuery): MutableList<LanePlanSchedule> {
    return query.apply(store.list())
}
