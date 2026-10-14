package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default cross dock schedules by code. */
interface DefaultCrossDockScheduleStore {
    fun find(code: String): DefaultCrossDockSchedule?
    fun save(item: DefaultCrossDockSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCrossDockSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCrossDockScheduleStore(
    var items: MutableMap<String, DefaultCrossDockSchedule>,
    var order: MutableList<String>,
) : DefaultCrossDockScheduleStore {
    override fun find(code: String): DefaultCrossDockSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCrossDockSchedule) {
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

    override fun list(): MutableList<DefaultCrossDockSchedule> {
        val out = mutableListOf<DefaultCrossDockSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCrossDockScheduleStore(): DefaultCrossDockScheduleStore {
    return MemoryDefaultCrossDockScheduleStore(
        mutableMapOf<String, DefaultCrossDockSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCrossDockScheduleStore(
    var inner: DefaultCrossDockScheduleStore,
    var cache: TtlCache<DefaultCrossDockSchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultCrossDockScheduleStore {
    override fun find(code: String): DefaultCrossDockSchedule? {
        val hit = (cache.get(code) as DefaultCrossDockSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCrossDockSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCrossDockSchedule> = inner.list()
}

fun newCachedDefaultCrossDockScheduleStore(inner: DefaultCrossDockScheduleStore, ttl: Int, clock: Clock): DefaultCrossDockScheduleStore {
    return CachedDefaultCrossDockScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultCrossDockSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cross dock schedules. Empty fields match all. */
data class DefaultCrossDockScheduleQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCrossDockSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DefaultCrossDockSchedule>): MutableList<DefaultCrossDockSchedule> {
        val matched = filter(items) { candidate: DefaultCrossDockSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCrossDockSchedules(store: DefaultCrossDockScheduleStore, query: DefaultCrossDockScheduleQuery): MutableList<DefaultCrossDockSchedule> {
    return query.apply(store.list())
}
