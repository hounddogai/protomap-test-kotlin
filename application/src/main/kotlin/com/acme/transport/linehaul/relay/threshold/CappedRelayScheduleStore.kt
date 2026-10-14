package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped relay schedules by code. */
interface CappedRelayScheduleStore {
    fun find(code: String): CappedRelaySchedule?
    fun save(item: CappedRelaySchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRelaySchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRelayScheduleStore(
    var items: MutableMap<String, CappedRelaySchedule>,
    var order: MutableList<String>,
) : CappedRelayScheduleStore {
    override fun find(code: String): CappedRelaySchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRelaySchedule) {
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

    override fun list(): MutableList<CappedRelaySchedule> {
        val out = mutableListOf<CappedRelaySchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRelayScheduleStore(): CappedRelayScheduleStore {
    return MemoryCappedRelayScheduleStore(mutableMapOf<String, CappedRelaySchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRelayScheduleStore(
    var inner: CappedRelayScheduleStore,
    var cache: TtlCache<CappedRelaySchedule>,
    var hits: Int,
    var misses: Int,
) : CappedRelayScheduleStore {
    override fun find(code: String): CappedRelaySchedule? {
        val hit = (cache.get(code) as CappedRelaySchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRelaySchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRelaySchedule> = inner.list()
}

fun newCachedCappedRelayScheduleStore(inner: CappedRelayScheduleStore, ttl: Int, clock: Clock): CappedRelayScheduleStore {
    return CachedCappedRelayScheduleStore(
        inner = inner,
        cache = newTtlCache<CappedRelaySchedule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped relay schedules. Empty fields match all. */
data class CappedRelayScheduleQuery(
    var region: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRelaySchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRelaySchedule>): MutableList<CappedRelaySchedule> {
        val matched = filter(items) { candidate: CappedRelaySchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRelaySchedules(store: CappedRelayScheduleStore, query: CappedRelayScheduleQuery): MutableList<CappedRelaySchedule> {
    return query.apply(store.list())
}
