package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge schedules by code. */
interface EvChargeScheduleStore {
    fun find(code: String): EvChargeSchedule?
    fun save(item: EvChargeSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeScheduleStore(
    var items: MutableMap<String, EvChargeSchedule>,
    var order: MutableList<String>,
) : EvChargeScheduleStore {
    override fun find(code: String): EvChargeSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeSchedule) {
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

    override fun list(): MutableList<EvChargeSchedule> {
        val out = mutableListOf<EvChargeSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeScheduleStore(): EvChargeScheduleStore {
    return MemoryEvChargeScheduleStore(mutableMapOf<String, EvChargeSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeScheduleStore(
    var inner: EvChargeScheduleStore,
    var cache: TtlCache<EvChargeSchedule>,
    var hits: Int,
    var misses: Int,
) : EvChargeScheduleStore {
    override fun find(code: String): EvChargeSchedule? {
        val hit = (cache.get(code) as EvChargeSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeSchedule> = inner.list()
}

fun newCachedEvChargeScheduleStore(inner: EvChargeScheduleStore, ttl: Int, clock: Clock): EvChargeScheduleStore {
    return CachedEvChargeScheduleStore(
        inner = inner,
        cache = newTtlCache<EvChargeSchedule>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge schedules. Empty fields match all. */
data class EvChargeScheduleQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeSchedule>): MutableList<EvChargeSchedule> {
        val matched = filter(items) { candidate: EvChargeSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeSchedules(store: EvChargeScheduleStore, query: EvChargeScheduleQuery): MutableList<EvChargeSchedule> {
    return query.apply(store.list())
}
