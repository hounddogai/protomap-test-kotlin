package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub schedules by code. */
interface HubScheduleStore {
    fun find(code: String): HubSchedule?
    fun save(item: HubSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubScheduleStore(
    var items: MutableMap<String, HubSchedule>,
    var order: MutableList<String>,
) : HubScheduleStore {
    override fun find(code: String): HubSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubSchedule) {
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

    override fun list(): MutableList<HubSchedule> {
        val out = mutableListOf<HubSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubScheduleStore(): HubScheduleStore {
    return MemoryHubScheduleStore(mutableMapOf<String, HubSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubScheduleStore(
    var inner: HubScheduleStore,
    var cache: TtlCache<HubSchedule>,
    var hits: Int,
    var misses: Int,
) : HubScheduleStore {
    override fun find(code: String): HubSchedule? {
        val hit = (cache.get(code) as HubSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubSchedule> = inner.list()
}

fun newCachedHubScheduleStore(inner: HubScheduleStore, ttl: Int, clock: Clock): HubScheduleStore {
    return CachedHubScheduleStore(
        inner = inner,
        cache = newTtlCache<HubSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub schedules. Empty fields match all. */
data class HubScheduleQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubSchedule): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubSchedule>): MutableList<HubSchedule> {
        val matched = filter(items) { candidate: HubSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubSchedules(store: HubScheduleStore, query: HubScheduleQuery): MutableList<HubSchedule> {
    return query.apply(store.list())
}
