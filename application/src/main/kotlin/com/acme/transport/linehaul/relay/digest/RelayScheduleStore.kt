package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay schedules by code. */
interface RelayScheduleStore {
    fun find(code: String): RelaySchedule?
    fun save(item: RelaySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelaySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayScheduleStore(
    var items: MutableMap<String, RelaySchedule>,
    var order: MutableList<String>,
) : RelayScheduleStore {
    override fun find(code: String): RelaySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelaySchedule) {
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

    override fun list(): MutableList<RelaySchedule> {
        val out = mutableListOf<RelaySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayScheduleStore(): RelayScheduleStore {
    return MemoryRelayScheduleStore(mutableMapOf<String, RelaySchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayScheduleStore(
    var inner: RelayScheduleStore,
    var cache: TtlCache<RelaySchedule>,
    var hits: Int,
    var misses: Int,
) : RelayScheduleStore {
    override fun find(code: String): RelaySchedule? {
        val hit = (cache.get(code) as RelaySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelaySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelaySchedule> = inner.list()
}

fun newCachedRelayScheduleStore(inner: RelayScheduleStore, ttl: Int, clock: Clock): RelayScheduleStore {
    return CachedRelayScheduleStore(
        inner = inner,
        cache = newTtlCache<RelaySchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay schedules. Empty fields match all. */
data class RelayScheduleQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelaySchedule): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelaySchedule>): MutableList<RelaySchedule> {
        val matched = filter(items) { candidate: RelaySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelaySchedules(store: RelayScheduleStore, query: RelayScheduleQuery): MutableList<RelaySchedule> {
    return query.apply(store.list())
}
