package com.acme.transport.customs.hold.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default hold schedules by code. */
interface DefaultHoldScheduleStore {
    fun find(code: String): DefaultHoldSchedule?
    fun save(item: DefaultHoldSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHoldSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHoldScheduleStore(
    var items: MutableMap<String, DefaultHoldSchedule>,
    var order: MutableList<String>,
) : DefaultHoldScheduleStore {
    override fun find(code: String): DefaultHoldSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHoldSchedule) {
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

    override fun list(): MutableList<DefaultHoldSchedule> {
        val out = mutableListOf<DefaultHoldSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHoldScheduleStore(): DefaultHoldScheduleStore {
    return MemoryDefaultHoldScheduleStore(mutableMapOf<String, DefaultHoldSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHoldScheduleStore(
    var inner: DefaultHoldScheduleStore,
    var cache: TtlCache<DefaultHoldSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultHoldScheduleStore {
    override fun find(code: String): DefaultHoldSchedule? {
        val hit = (cache.get(code) as DefaultHoldSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHoldSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHoldSchedule> = inner.list()
}

fun newCachedDefaultHoldScheduleStore(inner: DefaultHoldScheduleStore, ttl: Int, clock: Clock): DefaultHoldScheduleStore {
    return CachedDefaultHoldScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultHoldSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hold schedules. Empty fields match all. */
data class DefaultHoldScheduleQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHoldSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultHoldSchedule>): MutableList<DefaultHoldSchedule> {
        val matched = filter(items) { candidate: DefaultHoldSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHoldSchedules(store: DefaultHoldScheduleStore, query: DefaultHoldScheduleQuery): MutableList<DefaultHoldSchedule> {
    return query.apply(store.list())
}
