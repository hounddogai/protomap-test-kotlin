package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door schedules by code. */
interface StrictDockDoorScheduleStore {
    fun find(code: String): StrictDockDoorSchedule?
    fun save(item: StrictDockDoorSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorScheduleStore(
    var items: MutableMap<String, StrictDockDoorSchedule>,
    var order: MutableList<String>,
) : StrictDockDoorScheduleStore {
    override fun find(code: String): StrictDockDoorSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorSchedule) {
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

    override fun list(): MutableList<StrictDockDoorSchedule> {
        val out = mutableListOf<StrictDockDoorSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorScheduleStore(): StrictDockDoorScheduleStore {
    return MemoryStrictDockDoorScheduleStore(
        mutableMapOf<String, StrictDockDoorSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorScheduleStore(
    var inner: StrictDockDoorScheduleStore,
    var cache: TtlCache<StrictDockDoorSchedule>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorScheduleStore {
    override fun find(code: String): StrictDockDoorSchedule? {
        val hit = (cache.get(code) as StrictDockDoorSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorSchedule> = inner.list()
}

fun newCachedStrictDockDoorScheduleStore(inner: StrictDockDoorScheduleStore, ttl: Int, clock: Clock): StrictDockDoorScheduleStore {
    return CachedStrictDockDoorScheduleStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door schedules. Empty fields match all. */
data class StrictDockDoorScheduleQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDockDoorSchedule>): MutableList<StrictDockDoorSchedule> {
        val matched = filter(items) { candidate: StrictDockDoorSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorSchedules(store: StrictDockDoorScheduleStore, query: StrictDockDoorScheduleQuery): MutableList<StrictDockDoorSchedule> {
    return query.apply(store.list())
}
