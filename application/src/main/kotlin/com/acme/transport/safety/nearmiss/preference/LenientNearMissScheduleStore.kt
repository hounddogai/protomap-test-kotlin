package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient near miss schedules by code. */
interface LenientNearMissScheduleStore {
    fun find(code: String): LenientNearMissSchedule?
    fun save(item: LenientNearMissSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNearMissSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNearMissScheduleStore(
    var items: MutableMap<String, LenientNearMissSchedule>,
    var order: MutableList<String>,
) : LenientNearMissScheduleStore {
    override fun find(code: String): LenientNearMissSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNearMissSchedule) {
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

    override fun list(): MutableList<LenientNearMissSchedule> {
        val out = mutableListOf<LenientNearMissSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNearMissScheduleStore(): LenientNearMissScheduleStore {
    return MemoryLenientNearMissScheduleStore(
        mutableMapOf<String, LenientNearMissSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNearMissScheduleStore(
    var inner: LenientNearMissScheduleStore,
    var cache: TtlCache<LenientNearMissSchedule>,
    var hits: Int,
    var misses: Int,
) : LenientNearMissScheduleStore {
    override fun find(code: String): LenientNearMissSchedule? {
        val hit = (cache.get(code) as LenientNearMissSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNearMissSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNearMissSchedule> = inner.list()
}

fun newCachedLenientNearMissScheduleStore(inner: LenientNearMissScheduleStore, ttl: Int, clock: Clock): LenientNearMissScheduleStore {
    return CachedLenientNearMissScheduleStore(
        inner = inner,
        cache = newTtlCache<LenientNearMissSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient near miss schedules. Empty fields match all. */
data class LenientNearMissScheduleQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNearMissSchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientNearMissSchedule>): MutableList<LenientNearMissSchedule> {
        val matched = filter(items) { candidate: LenientNearMissSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNearMissSchedules(store: LenientNearMissScheduleStore, query: LenientNearMissScheduleQuery): MutableList<LenientNearMissSchedule> {
    return query.apply(store.list())
}
