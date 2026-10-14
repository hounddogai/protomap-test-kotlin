package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dwell schedules by code. */
interface CappedDwellScheduleStore {
    fun find(code: String): CappedDwellSchedule?
    fun save(item: CappedDwellSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDwellSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDwellScheduleStore(
    var items: MutableMap<String, CappedDwellSchedule>,
    var order: MutableList<String>,
) : CappedDwellScheduleStore {
    override fun find(code: String): CappedDwellSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDwellSchedule) {
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

    override fun list(): MutableList<CappedDwellSchedule> {
        val out = mutableListOf<CappedDwellSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDwellScheduleStore(): CappedDwellScheduleStore {
    return MemoryCappedDwellScheduleStore(mutableMapOf<String, CappedDwellSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDwellScheduleStore(
    var inner: CappedDwellScheduleStore,
    var cache: TtlCache<CappedDwellSchedule>,
    var hits: Int,
    var misses: Int,
) : CappedDwellScheduleStore {
    override fun find(code: String): CappedDwellSchedule? {
        val hit = (cache.get(code) as CappedDwellSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDwellSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDwellSchedule> = inner.list()
}

fun newCachedCappedDwellScheduleStore(inner: CappedDwellScheduleStore, ttl: Int, clock: Clock): CappedDwellScheduleStore {
    return CachedCappedDwellScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedDwellSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dwell schedules. Empty fields match all. */
data class CappedDwellScheduleQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDwellSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDwellSchedule>): MutableList<CappedDwellSchedule> {
        val matched = filter(items) { candidate: CappedDwellSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDwellSchedules(store: CappedDwellScheduleStore, query: CappedDwellScheduleQuery): MutableList<CappedDwellSchedule> {
    return query.apply(store.list())
}
