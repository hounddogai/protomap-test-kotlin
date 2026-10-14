package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dock door schedules by code. */
interface DefaultDockDoorScheduleStore {
    fun find(code: String): DefaultDockDoorSchedule?
    fun save(item: DefaultDockDoorSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDockDoorSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDockDoorScheduleStore(
    var items: MutableMap<String, DefaultDockDoorSchedule>,
    var order: MutableList<String>,
) : DefaultDockDoorScheduleStore {
    override fun find(code: String): DefaultDockDoorSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDockDoorSchedule) {
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

    override fun list(): MutableList<DefaultDockDoorSchedule> {
        val out = mutableListOf<DefaultDockDoorSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDockDoorScheduleStore(): DefaultDockDoorScheduleStore {
    return MemoryDefaultDockDoorScheduleStore(
        mutableMapOf<String, DefaultDockDoorSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDockDoorScheduleStore(
    var inner: DefaultDockDoorScheduleStore,
    var cache: TtlCache<DefaultDockDoorSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultDockDoorScheduleStore {
    override fun find(code: String): DefaultDockDoorSchedule? {
        val hit = (cache.get(code) as DefaultDockDoorSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDockDoorSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDockDoorSchedule> = inner.list()
}

fun newCachedDefaultDockDoorScheduleStore(inner: DefaultDockDoorScheduleStore, ttl: Int, clock: Clock): DefaultDockDoorScheduleStore {
    return CachedDefaultDockDoorScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultDockDoorSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dock door schedules. Empty fields match all. */
data class DefaultDockDoorScheduleQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDockDoorSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDockDoorSchedule>): MutableList<DefaultDockDoorSchedule> {
        val matched = filter(items) { candidate: DefaultDockDoorSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDockDoorSchedules(store: DefaultDockDoorScheduleStore, query: DefaultDockDoorScheduleQuery): MutableList<DefaultDockDoorSchedule> {
    return query.apply(store.list())
}
