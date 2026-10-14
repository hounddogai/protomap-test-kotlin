package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell schedules by code. */
interface DwellScheduleStore {
    fun find(code: String): DwellSchedule?
    fun save(item: DwellSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellScheduleStore(
    var items: MutableMap<String, DwellSchedule>,
    var order: MutableList<String>,
) : DwellScheduleStore {
    override fun find(code: String): DwellSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellSchedule) {
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

    override fun list(): MutableList<DwellSchedule> {
        val out = mutableListOf<DwellSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellScheduleStore(): DwellScheduleStore {
    return MemoryDwellScheduleStore(mutableMapOf<String, DwellSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellScheduleStore(
    var inner: DwellScheduleStore,
    var cache: TtlCache<DwellSchedule>,
    var hits: Int,
    var misses: Int,
) : DwellScheduleStore {
    override fun find(code: String): DwellSchedule? {
        val hit = (cache.get(code) as DwellSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellSchedule> = inner.list()
}

fun newCachedDwellScheduleStore(inner: DwellScheduleStore, ttl: Int, clock: Clock): DwellScheduleStore {
    return CachedDwellScheduleStore(
        inner = inner,
        cache = newTtlCache<DwellSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell schedules. Empty fields match all. */
data class DwellScheduleQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellSchedule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellSchedule>): MutableList<DwellSchedule> {
        val matched = filter(items) { candidate: DwellSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellSchedules(store: DwellScheduleStore, query: DwellScheduleQuery): MutableList<DwellSchedule> {
    return query.apply(store.list())
}
