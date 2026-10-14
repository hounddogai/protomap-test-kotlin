package com.acme.transport.fleetops.tire.plan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tire schedules by code. */
interface PendingTireScheduleStore {
    fun find(code: String): PendingTireSchedule?
    fun save(item: PendingTireSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTireSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTireScheduleStore(
    var items: MutableMap<String, PendingTireSchedule>,
    var order: MutableList<String>,
) : PendingTireScheduleStore {
    override fun find(code: String): PendingTireSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTireSchedule) {
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

    override fun list(): MutableList<PendingTireSchedule> {
        val out = mutableListOf<PendingTireSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTireScheduleStore(): PendingTireScheduleStore {
    return MemoryPendingTireScheduleStore(mutableMapOf<String, PendingTireSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTireScheduleStore(
    var inner: PendingTireScheduleStore,
    var cache: TtlCache<PendingTireSchedule>,
    var hits: Int,
    var misses: Int,
) : PendingTireScheduleStore {
    override fun find(code: String): PendingTireSchedule? {
        val hit = (cache.get(code) as PendingTireSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTireSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTireSchedule> = inner.list()
}

fun newCachedPendingTireScheduleStore(inner: PendingTireScheduleStore, ttl: Int, clock: Clock): PendingTireScheduleStore {
    return CachedPendingTireScheduleStore(
        inner = inner,
        cache = newTtlCache<PendingTireSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tire schedules. Empty fields match all. */
data class PendingTireScheduleQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTireSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PendingTireSchedule>): MutableList<PendingTireSchedule> {
        val matched = filter(items) { candidate: PendingTireSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTireSchedules(store: PendingTireScheduleStore, query: PendingTireScheduleQuery): MutableList<PendingTireSchedule> {
    return query.apply(store.list())
}
