package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual trailer yard schedules by code. */
interface ManualTrailerYardScheduleStore {
    fun find(code: String): ManualTrailerYardSchedule?
    fun save(item: ManualTrailerYardSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerYardSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerYardScheduleStore(
    var items: MutableMap<String, ManualTrailerYardSchedule>,
    var order: MutableList<String>,
) : ManualTrailerYardScheduleStore {
    override fun find(code: String): ManualTrailerYardSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerYardSchedule) {
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

    override fun list(): MutableList<ManualTrailerYardSchedule> {
        val out = mutableListOf<ManualTrailerYardSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerYardScheduleStore(): ManualTrailerYardScheduleStore {
    return MemoryManualTrailerYardScheduleStore(
        mutableMapOf<String, ManualTrailerYardSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerYardScheduleStore(
    var inner: ManualTrailerYardScheduleStore,
    var cache: TtlCache<ManualTrailerYardSchedule>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerYardScheduleStore {
    override fun find(code: String): ManualTrailerYardSchedule? {
        val hit = (cache.get(code) as ManualTrailerYardSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerYardSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerYardSchedule> = inner.list()
}

fun newCachedManualTrailerYardScheduleStore(inner: ManualTrailerYardScheduleStore, ttl: Int, clock: Clock): ManualTrailerYardScheduleStore {
    return CachedManualTrailerYardScheduleStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerYardSchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer yard schedules. Empty fields match all. */
data class ManualTrailerYardScheduleQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerYardSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerYardSchedule>): MutableList<ManualTrailerYardSchedule> {
        val matched = filter(items) { candidate: ManualTrailerYardSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerYardSchedules(store: ManualTrailerYardScheduleStore, query: ManualTrailerYardScheduleQuery): MutableList<ManualTrailerYardSchedule> {
    return query.apply(store.list())
}
