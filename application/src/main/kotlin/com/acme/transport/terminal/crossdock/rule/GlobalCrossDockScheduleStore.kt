package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock schedules by code. */
interface GlobalCrossDockScheduleStore {
    fun find(code: String): GlobalCrossDockSchedule?
    fun save(item: GlobalCrossDockSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockScheduleStore(
    var items: MutableMap<String, GlobalCrossDockSchedule>,
    var order: MutableList<String>,
) : GlobalCrossDockScheduleStore {
    override fun find(code: String): GlobalCrossDockSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockSchedule) {
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

    override fun list(): MutableList<GlobalCrossDockSchedule> {
        val out = mutableListOf<GlobalCrossDockSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockScheduleStore(): GlobalCrossDockScheduleStore {
    return MemoryGlobalCrossDockScheduleStore(
        mutableMapOf<String, GlobalCrossDockSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockScheduleStore(
    var inner: GlobalCrossDockScheduleStore,
    var cache: TtlCache<GlobalCrossDockSchedule>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockScheduleStore {
    override fun find(code: String): GlobalCrossDockSchedule? {
        val hit = (cache.get(code) as GlobalCrossDockSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockSchedule> = inner.list()
}

fun newCachedGlobalCrossDockScheduleStore(inner: GlobalCrossDockScheduleStore, ttl: Int, clock: Clock): GlobalCrossDockScheduleStore {
    return CachedGlobalCrossDockScheduleStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockSchedule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock schedules. Empty fields match all. */
data class GlobalCrossDockScheduleQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockSchedule): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCrossDockSchedule>): MutableList<GlobalCrossDockSchedule> {
        val matched = filter(items) { candidate: GlobalCrossDockSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockSchedules(store: GlobalCrossDockScheduleStore, query: GlobalCrossDockScheduleQuery): MutableList<GlobalCrossDockSchedule> {
    return query.apply(store.list())
}
