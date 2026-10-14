package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered incident schedules by code. */
interface TieredIncidentScheduleStore {
    fun find(code: String): TieredIncidentSchedule?
    fun save(item: TieredIncidentSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIncidentSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIncidentScheduleStore(
    var items: MutableMap<String, TieredIncidentSchedule>,
    var order: MutableList<String>,
) : TieredIncidentScheduleStore {
    override fun find(code: String): TieredIncidentSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIncidentSchedule) {
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

    override fun list(): MutableList<TieredIncidentSchedule> {
        val out = mutableListOf<TieredIncidentSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIncidentScheduleStore(): TieredIncidentScheduleStore {
    return MemoryTieredIncidentScheduleStore(
        mutableMapOf<String, TieredIncidentSchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIncidentScheduleStore(
    var inner: TieredIncidentScheduleStore,
    var cache: TtlCache<TieredIncidentSchedule>,
    var hits: Int,
    var misses: Int,
) : TieredIncidentScheduleStore {
    override fun find(code: String): TieredIncidentSchedule? {
        val hit = (cache.get(code) as TieredIncidentSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIncidentSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIncidentSchedule> = inner.list()
}

fun newCachedTieredIncidentScheduleStore(inner: TieredIncidentScheduleStore, ttl: Int, clock: Clock): TieredIncidentScheduleStore {
    return CachedTieredIncidentScheduleStore(
        inner = inner,
        cache = newTtlCache<TieredIncidentSchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered incident schedules. Empty fields match all. */
data class TieredIncidentScheduleQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIncidentSchedule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<TieredIncidentSchedule>): MutableList<TieredIncidentSchedule> {
        val matched = filter(items) { candidate: TieredIncidentSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIncidentSchedules(store: TieredIncidentScheduleStore, query: TieredIncidentScheduleQuery): MutableList<TieredIncidentSchedule> {
    return query.apply(store.list())
}
