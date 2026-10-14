package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door schedules by code. */
interface DockDoorScheduleStore {
    fun find(code: String): DockDoorSchedule?
    fun save(item: DockDoorSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorScheduleStore(
    var items: MutableMap<String, DockDoorSchedule>,
    var order: MutableList<String>,
) : DockDoorScheduleStore {
    override fun find(code: String): DockDoorSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorSchedule) {
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

    override fun list(): MutableList<DockDoorSchedule> {
        val out = mutableListOf<DockDoorSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorScheduleStore(): DockDoorScheduleStore {
    return MemoryDockDoorScheduleStore(mutableMapOf<String, DockDoorSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorScheduleStore(
    var inner: DockDoorScheduleStore,
    var cache: TtlCache<DockDoorSchedule>,
    var hits: Int,
    var misses: Int,
) : DockDoorScheduleStore {
    override fun find(code: String): DockDoorSchedule? {
        val hit = (cache.get(code) as DockDoorSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorSchedule> = inner.list()
}

fun newCachedDockDoorScheduleStore(inner: DockDoorScheduleStore, ttl: Int, clock: Clock): DockDoorScheduleStore {
    return CachedDockDoorScheduleStore(
        inner = inner,
        cache = newTtlCache<DockDoorSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door schedules. Empty fields match all. */
data class DockDoorScheduleQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorSchedule>): MutableList<DockDoorSchedule> {
        val matched = filter(items) { candidate: DockDoorSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorSchedules(store: DockDoorScheduleStore, query: DockDoorScheduleQuery): MutableList<DockDoorSchedule> {
    return query.apply(store.list())
}
