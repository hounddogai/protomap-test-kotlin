package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient hold schedules by code. */
interface LenientHoldScheduleStore {
    fun find(code: String): LenientHoldSchedule?
    fun save(item: LenientHoldSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHoldSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHoldScheduleStore(
    var items: MutableMap<String, LenientHoldSchedule>,
    var order: MutableList<String>,
) : LenientHoldScheduleStore {
    override fun find(code: String): LenientHoldSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHoldSchedule) {
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

    override fun list(): MutableList<LenientHoldSchedule> {
        val out = mutableListOf<LenientHoldSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHoldScheduleStore(): LenientHoldScheduleStore {
    return MemoryLenientHoldScheduleStore(mutableMapOf<String, LenientHoldSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHoldScheduleStore(
    var inner: LenientHoldScheduleStore,
    var cache: TtlCache<LenientHoldSchedule>,
    var hits: Int,
    var misses: Int,
) : LenientHoldScheduleStore {
    override fun find(code: String): LenientHoldSchedule? {
        val hit = (cache.get(code) as LenientHoldSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHoldSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHoldSchedule> = inner.list()
}

fun newCachedLenientHoldScheduleStore(inner: LenientHoldScheduleStore, ttl: Int, clock: Clock): LenientHoldScheduleStore {
    return CachedLenientHoldScheduleStore(
        inner = inner,
        cache = newTtlCache<LenientHoldSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hold schedules. Empty fields match all. */
data class LenientHoldScheduleQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHoldSchedule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientHoldSchedule>): MutableList<LenientHoldSchedule> {
        val matched = filter(items) { candidate: LenientHoldSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHoldSchedules(store: LenientHoldScheduleStore, query: LenientHoldScheduleQuery): MutableList<LenientHoldSchedule> {
    return query.apply(store.list())
}
