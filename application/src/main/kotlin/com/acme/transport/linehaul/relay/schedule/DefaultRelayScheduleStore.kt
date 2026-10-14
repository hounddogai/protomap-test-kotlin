package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default relay schedules by code. */
interface DefaultRelayScheduleStore {
    fun find(code: String): DefaultRelaySchedule?
    fun save(item: DefaultRelaySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRelaySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRelayScheduleStore(
    var items: MutableMap<String, DefaultRelaySchedule>,
    var order: MutableList<String>,
) : DefaultRelayScheduleStore {
    override fun find(code: String): DefaultRelaySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRelaySchedule) {
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

    override fun list(): MutableList<DefaultRelaySchedule> {
        val out = mutableListOf<DefaultRelaySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRelayScheduleStore(): DefaultRelayScheduleStore {
    return MemoryDefaultRelayScheduleStore(
        mutableMapOf<String, DefaultRelaySchedule>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRelayScheduleStore(
    var inner: DefaultRelayScheduleStore,
    var cache: TtlCache<DefaultRelaySchedule>,
    var hits: Int,
    var misses: Int,
) : DefaultRelayScheduleStore {
    override fun find(code: String): DefaultRelaySchedule? {
        val hit = (cache.get(code) as DefaultRelaySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRelaySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRelaySchedule> = inner.list()
}

fun newCachedDefaultRelayScheduleStore(inner: DefaultRelayScheduleStore, ttl: Int, clock: Clock): DefaultRelayScheduleStore {
    return CachedDefaultRelayScheduleStore(
        inner = inner,
        cache = newTtlCache<DefaultRelaySchedule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default relay schedules. Empty fields match all. */
data class DefaultRelayScheduleQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRelaySchedule): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRelaySchedule>): MutableList<DefaultRelaySchedule> {
        val matched = filter(items) { candidate: DefaultRelaySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRelaySchedules(store: DefaultRelayScheduleStore, query: DefaultRelayScheduleQuery): MutableList<DefaultRelaySchedule> {
    return query.apply(store.list())
}
